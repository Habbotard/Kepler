#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

#include "database/queries/player_query.h"
#include "game/room/pool/pool_handler.h"

void SWIMSUIT(player *player, incoming_message *message) {
    char *content = im_get_content(message);

    if (content == NULL) {
        return;
    }

    if (player->room_user->room == NULL) {
        goto cleanup;
    }

    // Update pool figure
    free(player->player_data->pool_figure);
    player->player_data->pool_figure = strdup(content);

    // Refresh pool figure
    outgoing_message *refresh = om_create(28); // "@\"
    append_user_list(refresh, player);
    room_send(player->room_user->room, refresh);

    // Call handler to exit booth
    pool_booth_exit(player);

    // Save looks to database
    query_player_save_looks(player);

    cleanup:
        free(content);
}
