#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

#include "database/queries/item_query.h"

#include "game/inventory/inventory.h"

void GRPC(player *player, incoming_message *message) {
    char *content = im_get_content(message);

    char *page = get_argument(content, "\r", 1);
    char *sale_code = get_argument(content, "\r", 3);

    if (page == NULL) {
        goto cleanup;
    }

    if (sale_code == NULL) {
        goto cleanup;
    }

    catalogue_page *store_page = (catalogue_page *) catalogue_manager_get_page_by_index(page);

    if (store_page == NULL) {
        goto cleanup;
    }

    if (player->player_data->rank < store_page->min_role) {
        goto cleanup;
    }

    catalogue_item *store_item = (catalogue_item *) catalogue_manager_get_item(sale_code);

    if (store_item == NULL) {
        goto cleanup;
    }

    if (store_item->is_package) {
        goto cleanup; // TODO: Purchase packages
    }


    if (store_item->price > player->player_data->credits) {
        outgoing_message *om = om_create(68); // "AD"
        player_send(player, om);
        om_cleanup(om);

        goto cleanup;
    }

    char *custom_data = NULL;

    if (!store_item->is_package) {
        char *extra_data = get_argument(content, "\r", 4);

        if (store_item->definition->behaviour->is_decoration) {
            if (is_numeric(extra_data)) {
                custom_data = strdup(extra_data);
            }
        } else {
            if (store_item->item_specialspriteid > 0) {
                char num[10];
                sprintf(num, "%i", store_item->item_specialspriteid);
                custom_data = strdup(num);
            }
        }

        if (store_item->definition->behaviour->is_prize_trophy) {
            filter_vulnerable_characters(&extra_data, true);

            char *short_date = get_short_time_formatted();

            stringbuilder *sb = sb_create();
            sb_add_string(sb, player->player_data->username);
            sb_add_char(sb, 9);
            sb_add_string(sb, short_date);
            sb_add_char(sb, 9);
            sb_add_string(sb, extra_data);

            custom_data = strdup(sb->data);

            sb_cleanup(sb);
            free(short_date);
        }

        free(extra_data);
    }

    player->player_data->credits -= store_item->price;
    player_send_credits(player);

    int item_id = item_query_create(player->player_data->id, 0, store_item->definition->id, 0, 0, 0, 0, custom_data);
    item *inventory_item = item_create(item_id, 0, store_item->definition->id, 0, 0, 0, 0, custom_data);

    list_add(player->inventory->items, inventory_item);
    player_query_save_currency(player);

    inventory *inv = (inventory *) player->inventory;
    inventory_send(inv, "update", player);

    cleanup:
        if (content != NULL) {
            free(content);
        }

        if (page != NULL) {
            free(page);
        }

        if (sale_code != NULL) {
            free(sale_code);
        }
}