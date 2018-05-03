#include <stdbool.h>

#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

#include "game/player/player.h"

#include "game/room/room.h"
#include "game/room/room_user.h"

void SHOUT(session *player, incoming_message *im) {
    if (player->room_user->room == NULL) {
        return;
    }

    char *message = im_read_str(im);

    if (message != NULL) {
        filter_vulnerable_characters(&message, true);

        // Process command
        if (room_user_process_command((room_user *) player->room_user, message)) {
            // Send cancel typing packet to room
            outgoing_message *om = om_create(361); // "Ei"
            om_write_int(om, player->room_user->instance_id);
            om_write_int(om, 0);
            room_send(player->room_user->room, om);
            goto cleanup;
        }

        room_user_process_gesture((room_user *) player->room_user, message);

        outgoing_message *om = om_create(26); // "@Z"
        om_write_int(om, player->room_user->instance_id);
        om_write_str(om, message);
        room_send(player->room_user->room, om);
    }

    cleanup:
        free(message);
}
