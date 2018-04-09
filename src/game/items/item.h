#ifndef ITEM_H
#define ITEM_H

typedef struct coord_s coord;

typedef struct item_s {
    int id;
    int room_id;
    char *class_name;
    int is_table;
    int sprite_id;
    coord *coords;
    char *custom_data;
    char *current_program;
    char *current_program_state;
    int can_sit;
    int is_solid;
} item;

item *item_create(int, int, char*, int, int, int, double, int, char*);
void item_assign_program(item*, char*);
void item_dispose(item *item);

#endif