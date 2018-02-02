#ifndef MESSAGE_HANDLER_H
#define MESSAGE_HANDLER_H

#define MESSAGES 9999

typedef struct incoming_message_s incoming_message;
typedef struct player_s player;

typedef void (*mh_request)(player*, incoming_message*);
mh_request message_requests[MESSAGES];

void mh_invoke_message(incoming_message*, player*);
void mh_add_messages();

#endif