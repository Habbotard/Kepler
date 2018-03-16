#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "list.h"

#include "communication/messages/outgoing_message.h"
#include "game/messenger/messenger_friend.h"

#include "game/player/player.h"
#include "game/player/player_manager.h"

#include "game/room/room.h"
#include "game/room/room_user.h"
#include "game/room/room_manager.h"

#include "shared.h"

messenger_entry *messenger_entry_create(int friend_id) {
    messenger_entry *friend = malloc(sizeof(messenger_entry));
    friend->friend_id = friend_id;
    return friend;
}

void messenger_entry_serialise(int user_id, outgoing_message *response) {
    player_data *data = player_manager_get_data_by_id(user_id);
    player *search_player = player_manager_find_by_id(user_id);

    if (data != NULL) {
        om_write_int(response, data->id);
        om_write_str(response, data->username);
        om_write_int(response, strcmp(data->sex, "M") == 0);
        om_write_str(response, data->console_motto);

        int is_online = (search_player != NULL);
        om_write_int(response, is_online);

        if (is_online) {
            if (search_player->room_user->room != NULL) {
                room *room = room_manager_get_by_id(search_player->room_user->room_id);

                if (list_size(room->public_items) > 0) {
                    om_write_str(response, room->room_data->name);
                } else {
                    om_write_str(response, "Floor1a");
                }
            } else {
                om_write_str(response, "On hotel view");
            }

        } else {
            om_write_str(response, "");
        }

        om_write_str(response, get_time_formatted_custom(data->last_online));
        om_write_str(response, data->figure);

        if (!is_online) {
            player_data_cleanup(data);
        }
    }
}

void messenger_entry_cleanup(messenger_entry *entry) {
    free(entry);
}