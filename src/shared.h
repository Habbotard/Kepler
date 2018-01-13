#ifndef SHARED_H
#define SHARED_H

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdbool.h>

#include "game/player/player_manager.h"
#include "game/room/room_manager.h"

#define PREFIX "Kepler"

struct server {
    struct player_manager player_manager;
    struct room_manager room_manager;
};

static struct server global;

int valid_password(const char *username, const char *password);
int get_name_check_code(char *username);
bool is_numeric(const char*);
bool has_numbers(const char*);
bool valid_string(char*, char*);
void print_info(const char *, ...);
void print_error(const char *, ...);

#endif