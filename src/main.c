#include <stdbool.h>
#include <sodium.h>
#include <signal.h>

#include "main.h"
#include "shared.h"

#include "list.h"
#include "log.h"

#include "server/server_listener.h"
#include "server/rcon/rcon_listener.h"

#include "communication/message_handler.h"
#include "database/db_connection.h"

#include "game/game_thread.h"

#include "util/threading.h"
#include "util/configuration/configuration.h"

int main(void) {
    //signal(SIGPIPE, SIG_IGN); // Stops the server crashing when the connection is closed immediately. Ignores signal 13.
    signal(SIGINT, exit_program); // Handle cleanup on Ctrl-C

    log_info("Kepler Habbo server...");
    log_info("Written by Quackster");

    configuration_init();

    if (configuration_get_bool("debug")) {
        log_set_level(LOG_DEBUG);
    } else {
        log_set_level(LOG_INFO);
    }

    // Always enable debug log level in debug builds
    // Release builds will use info log level
#ifndef NDEBUG
    log_set_level(LOG_DEBUG);

    log_debug("This is a debug build, meant for testing purposes");
#endif

    if (sodium_init() < 0) {
        log_fatal("Could not initialise password hashing library");
        return EXIT_FAILURE;
    }

    if (!db_initialise()) {
        return EXIT_FAILURE;
    }

    log_info("Initialising various game managers...");

    fuserights_init();
    walkways_init();
    texts_manager_init();
    player_manager_init();
    model_manager_init();
    category_manager_init();
    room_manager_init();
    item_manager_init();
    catalogue_manager_init();
    message_handler_init();
    create_thread_pool();
    room_manager_load_connected_rooms();

    server_settings rcon_settings, server_settings;
    uv_thread_t mus_thread, server_thread, game_thread;

    strcpy(rcon_settings.ip, configuration_get_string("rcon.ip.address"));
    rcon_settings.port = configuration_get_int("rcon.port");

    strcpy(server_settings.ip, configuration_get_string("server.ip.address"));
    server_settings.port = configuration_get_int("server.port");

    game_thread_init(&game_thread);
    start_rcon(&rcon_settings, &mus_thread);
    start_server(&server_settings, &server_thread);

    uv_thread_join(&mus_thread);
    uv_thread_join(&server_thread);
    uv_thread_join(&game_thread);

    return EXIT_SUCCESS;
}

/**
 * Exits program, calls dispose_program
 */
void exit_program() {
    dispose_program();
    exit(EXIT_SUCCESS);
}

/**
 * Destroys program, clears all memory, except server listen instances.
 */
void dispose_program() {
    log_info("Shutting down server!");
    global.is_shutdown = true;

    //thpool_destroy(global.thread_manager.pool);
    player_manager_dispose();
    catalogue_manager_dispose();
    category_manager_dispose();
    configuration_dispose();
    texts_manager_dispose();
    room_manager_dispose();
    model_manager_dispose();
    item_manager_dispose();

    if (sqlite3_close(global.DB) != SQLITE_OK) {
        log_fatal("Could not close SQLite database: %s", sqlite3_errmsg(global.DB));
    }

    log_info("Have a nice day!");
}
