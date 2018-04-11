#ifndef ITEM_DEFINITION_H
#define ITEM_DEFINITION_H

typedef struct item_definition_s {
    int id;
    int cast_directory;
    char *sprite;
    char *colour;
    int length;
    int width;
    double top_height;
    char *behaviour;
} item_definition;

item_definition *item_definition_create(int id, int cast_directory, char *sprite, char *colour, int length, int width, double top_height, char *behaviour);

#endif