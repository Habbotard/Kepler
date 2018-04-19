#ifndef ITEM_H
#define ITEM_H

typedef struct coord_s coord;
typedef struct item_definition_s item_definition;

typedef struct item_s {
    int id;
    int room_id;
    coord *coords;
    char *custom_data;
    char *current_program;
    char *current_program_state;
    item_definition *definition;
} item;

item *item_create(int id, int room_id, int definition_id, int x, int y, double z, int rotation, char *custom_data);
char *item_as_string(item *item);
char *item_strip_string(item *item, int strip_slot_id);
void item_assign_program(item*, char*);
double item_total_height(item *item);
void item_dispose(item *item);

#endif