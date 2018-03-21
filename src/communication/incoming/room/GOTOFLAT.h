#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

#include "game/room/room.h"

void GOTOFLAT(player *player, incoming_message *message) {
    char *content = im_get_content(message);

    if (!is_numeric(content)) {
        goto cleanup;
        return;
    }

    room *room = room_manager_get_by_id((int)strtol(content, NULL, 10));

    if (room != NULL) { 
        room_enter(room, player);
        room_load(room, player);
    }

    cleanup:
        free(content);
}
