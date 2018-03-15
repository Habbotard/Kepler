#ifndef MESSENGER_H
#define MESSENGER_H

typedef struct list_s List;
typedef struct player_s player;

typedef struct messenger_s {
    List *friends;
    List *requests;
} messenger;

messenger *messenger_create();
void messenger_init(player*);
int messenger_is_friends(messenger*, int);
void messenger_cleanup(messenger*);

#endif