#ifndef MESSENGER_GETREQUESTS_H
#define MESSENGER_GETREQUESTS_H

#include "list.h"

#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

void MESSENGER_GETREQUESTS(player *p, incoming_message *message) {
    for (int i = 0; i < list_size(p->messenger->requests); i++) {
        messenger_entry *request_entry;
        list_get_at(p->messenger->requests, i, (void*)&request_entry);

        char *friends_name = query_player_username(request_entry->friend_id);

        if (friends_name == NULL) {
            continue;
        }
        
        outgoing_message *request = om_create(132); // "BD"
        om_write_int(request, request_entry->friend_id);
        om_write_str(request, friends_name);
        player_send(p, request);
        om_cleanup(request);
        
        free(friends_name);
    }
}

#endif