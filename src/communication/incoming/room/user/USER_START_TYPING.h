#include <stdbool.h>

#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

#include "game/player/player.h"

#include "game/room/room.h"
#include "game/room/room_user.h"

void USER_START_TYPING(session *player, incoming_message *im) {
    if (player->room_user == NULL || player->room_user->is_typing) {
        return;
    }

    player->room_user->is_typing = true;

    room_user_reset_idle_timer(player->room_user);

    outgoing_message *om = om_create(361); // "Ei"
    om_write_int(om, player->room_user->instance_id);
    om_write_int(om, 1);
    room_send(player->room_user->room, om);
}
