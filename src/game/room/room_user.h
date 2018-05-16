#ifndef ROOM_USER_H
#define ROOM_USER_H

#include <stdbool.h>
#include <ctype.h>

#include "game/room/room.h"

typedef struct item_s item;
typedef struct deque_s Deque;
typedef struct coord_s coord;
typedef struct outgoing_message_s outgoing_message;
typedef struct hashtable_s HashTable;

typedef struct room_user_s {
    entity *entity;
    int authenticate_id;
    int instance_id;
    int room_id;
    struct room_s *room;
    coord *position;
    coord *goal;
    coord *next;
    Deque *walk_list;
    int is_walking;
    int is_typing;
    int needs_update;
    HashTable *statuses;
    bool walking_lock;
    bool is_diving;
    int lido_vote;
    int room_idle_timer;
    int room_look_at_timer;
} room_user;

typedef struct room_user_status_s {
    char *key;
    char *value;
    int sec_lifetime;
    int sec_action_switch;
    int sec_action_lifetime;
    int sec_switch_lifetime;
    char *action;
    int lifetime_countdown;
    int action_countdown;
    int action_switch_countdown;
} room_user_status;

room_user *room_user_create(entity*);
void walk_to(room_user*, int, int);
void stop_walking(room_user*, bool silent);
void room_user_reset_idle_timer(room_user *room_user);
void room_user_show_chat(room_user *room_user, char *text, bool is_shout);
void room_user_look(room_user *room_user, coord *towards);
bool room_user_process_command(room_user *room_user, char *text);
void room_user_invoke_item(room_user *room_user);
void room_user_clear_walk_list(room_user*);
void append_user_list(outgoing_message*, entity*);
void append_user_status(outgoing_message*, entity*);
void room_user_carry_item(room_user *room_user, int carry_id, char *carry_name);
void room_user_reset(room_user*, bool cleanup);
void room_user_add_status(room_user*,char*,char*,int,char*,int,int);
void room_user_remove_status(room_user*,char*);
int room_user_has_status(room_user*, char*);

#endif