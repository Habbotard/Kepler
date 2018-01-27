#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

#include "game/room/room_user.h"
#include "game/room/room_manager.h"

void TRYFLAT(player *player, incoming_message *message) {
    char *content = im_get_content(message);

    if (!is_numeric(content)) {
        free(content);
        return;
    }

    room *room = room_manager_get_by_id(strtol(content, NULL, 10));

    if (room != NULL) { 
        player->room_user->room = room;

        outgoing_message *interest = om_create(41); // "@i"
        player_send(player, interest);
        om_cleanup(interest);
    }

    free(content);
}
