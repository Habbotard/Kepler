#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

#include "database/queries/player_query.h"

void MESSENGER_ASSIGNPERSMSG(session *player, incoming_message *message) {
    if (player->player_data == NULL) {
        return;
    }

    char *console_motto = im_read_str(message);

    free(player->player_data->console_motto);
    player->player_data->console_motto = console_motto;

    outgoing_message *response = om_create(147); // "BS"
    om_write_str(response, player->player_data->console_motto);
    player_send(player, response);
    om_cleanup(response);

    player_query_save_motto(player);
}
