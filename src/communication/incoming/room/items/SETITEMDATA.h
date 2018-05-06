#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

#include "game/inventory/inventory.h"
#include "game/player/player.h"

#include "game/room/room.h"
#include "game/room/manager/room_item_manager.h"

#include "game/items/item.h"

void SETITEMDATA(session *player, incoming_message *message) {
    if (player->room_user->room == NULL) {
        return;
    }

    if (!room_has_rights(player->room_user->room, player->player_data->id)) {
        return;
    }

    char *item_id = NULL;
    char *colour = NULL;
    char *postit_content = NULL;

    char *content = im_get_content(message);

    if (content == NULL || strlen(content) <= 10) { // Make sure it includes the ID and colour
        return;
    }

    item_id = get_argument(content, "/", 0);

    if (!is_numeric(item_id)) {
        goto cleanup;
    }

    colour = strdup(content + strlen(item_id) + 1);
    colour[6] = '\0';

    if (strcmp(colour, "FFFFFF") == 0 ||
        strcmp(colour, "FFFF33") == 0 ||
        strcmp(colour, "FF9CFF") == 0 ||
        strcmp(colour, "9CFF9C") == 0 ||
        strcmp(colour, "9CCEFF") == 0) {

        postit_content = strdup(content + strlen(colour) + 3);

        if (strlen(postit_content) > 684) { // Limit messages to either 0 or maximum of 684 characters
            char msg[685];
            memcpy(msg, &postit_content[0], 684);
            msg[684] = '\0';

            free(postit_content);
            postit_content = strdup(msg);
        }

        stringbuilder *sb = sb_create();
        sb_add_string(sb, colour);
        sb_add_string(sb, postit_content);

        item *item = room_item_manager_get(player->room_user->room, (int) strtol(item_id, NULL, 10));
        item_set_custom_data(item, strdup(sb->data));
        item_query_save(item);

        sb_cleanup(sb);
    } else {
        send_alert(player, "No scripters allowed, bye bye!");
        player_disconnect(player);
    }

    cleanup:
    free(item_id);
    free(colour);
    free(content);
}