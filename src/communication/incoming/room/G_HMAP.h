#include "game/room/mapping/room_model.h"

#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

void G_HMAP(session *player, incoming_message *message) {
    if (player->room_user->room == NULL) {
        return;
    }

    if (player->room_user->room->room_data->model_data == NULL) {
        printf("Room %i has invalid model data.\n", player->room_user->room->room_data->id);
        return;
    }

    outgoing_message *om = om_create(31); // "@_"
    om_write_str(om, player->room_user->room->room_data->model_data->heightmap);
    player_send(player, om);
    om_cleanup(om);
}
