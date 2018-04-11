#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "item_definition.h"
#include "item_behaviour.h"

item_definition *item_definition_create(int id, int cast_directory, char *sprite, char *colour, int length, int width, double top_height, char *behaviour) {
    item_definition *def = malloc(sizeof(item_definition));
    def->id = id;
    def->cast_directory = cast_directory;
    def->sprite = strdup(sprite);
    def->colour = strdup(colour);
    def->length = length;
    def->width = width;
    def->top_height = top_height;
    def->behaviour_data = strdup(behaviour);
    def->behaviour = item_behaviour_parse(def);

    if (def->behaviour->isWallItem) {
        printf("def: %s\n", def->sprite);
    }

    return def;
}