#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

#include "game/inventory/inventory.h"
#include "game/player/player.h"

#include "game/room/room.h"
#include "game/items/item.h"

#include "database/queries/room_query.h"
#include "database/queries/item_query.h"

void FLATPROPBYITEM(player *player, incoming_message *message) {
    if (player->room_user->room == NULL) {
        return;
    }

    room *room = player->room_user->room;

    if (!room_has_rights(room, player->player_data->id)) {
        return;
    }

    char *content = im_get_content(message);

    if (content == NULL) {
        return;
    }

    char *str_mode = get_argument(content, "/", 0);
    char *str_id = get_argument(content, "/", 1);

    if (str_id == NULL || str_mode == NULL) {
        goto cleanup;
    }

    inventory *inv = (inventory*) player->inventory;
    item *item = inventory_get_item(inv, (int)strtol(str_id, NULL, 10));

    if (item == NULL || !item->definition->behaviour->is_decoration) {
        goto cleanup;
    }

    if (strcmp(str_mode, "wallpaper") == 0) {
        room->room_data->wallpaper = (int)strtol(item->custom_data, NULL, 10);
    } else {
        room->room_data->floor = (int)strtol(item->custom_data, NULL, 10);
    }

    outgoing_message *om = om_create(46); // "@n"
    sb_add_string(om->sb, str_mode);
    sb_add_string(om->sb, "/");
    sb_add_string(om->sb, item->custom_data);
    room_send(room, om);

    list_remove(inv->items, item, NULL);

    query_room_save(room);
    item_query_delete(item->id);

    cleanup:
        free(content);

        if (str_mode != NULL) {
            free(str_mode);
        }

        if (str_id != NULL) {
            free(str_id);
        }
}
