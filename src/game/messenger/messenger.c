#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "list.h"

#include "communication/messages/outgoing_message.h"

#include "game/player/player.h"

#include "game/messenger/messenger.h"
#include "game/messenger/messenger_friend.h"

#include "database/queries/messenger_query.h"

messenger *messenger_create() {
    messenger *messenger_manager = malloc(sizeof(messenger));
    messenger_manager->friends = NULL;
    return messenger_manager;
}

void messenger_init(player *player) {
    List *friends = messenger_query_get_friends(player->player_data->id);
    player->messenger->friends = friends;

    /*if (messenger_is_friends(player->messenger, 1)) {
        printf("PLAYER IS FRIEND!\n");
    }*/
}

int messenger_is_friends(messenger *messenger, int user_id) {
    for (int i = 0; i < list_size(messenger->friends); i++) {
        messenger_friend *friend;
        list_get_at(messenger->friends, i, (void*)&friend);

        if (friend->friend_id == user_id) {
            return 1;
        }
    }

    return 0;
}

void messenger_cleanup(messenger *messenger_manager) {
    if (messenger_manager->friends != NULL) {
        list_destroy(messenger_manager->friends);
    }

    free(messenger_manager);
}