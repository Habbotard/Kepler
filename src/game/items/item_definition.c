#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "item_definition.h"
#include "item_behaviour.h"
#include "util/stringbuilder.h"

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
    return def;
}

char *item_definition_get_name(item_definition *definition, int special_sprite_id) {
    if (definition->behaviour->isDecoration) {
        return strdup(definition->sprite);
    } else {
        char *external_text_key = item_definition_get_text_key(definition, special_sprite_id);

        stringbuilder *sb = sb_create();
        sb_add_string(sb, external_text_key);
        sb_add_string(sb, "_name");

        char *name = strdup(sb->data);
        sb_cleanup(sb);

        return name;
    }
}

char *item_definition_get_desc(item_definition *definition, int special_sprite_id) {
    if (definition->behaviour->isDecoration) {
        return strdup(definition->sprite);
    } else {
        char *external_text_key = item_definition_get_text_key(definition, special_sprite_id);

        stringbuilder *sb = sb_create();
        sb_add_string(sb, external_text_key);
        sb_add_string(sb, "_desc");

        char *name = strdup(sb->data);
        sb_cleanup(sb);

        return name;
    }
}

char *item_definition_get_icon(item_definition *definition, int special_sprite_id) {
    stringbuilder *sb = sb_create();
    sb_add_string(sb, definition->sprite);
    sb_add_string(sb, " ");
    sb_add_int(sb, special_sprite_id);

    char *name = strdup(sb->data);
    sb_cleanup(sb);

    return name;
}


char *item_definition_get_text_key(item_definition *definition, int special_sprite_id) {
    stringbuilder *sb = sb_create();

    if (special_sprite_id == 0) {
        if (definition->behaviour->isWallItem) {
            sb_add_string(sb, "wallitem");
        } else {
            sb_add_string(sb, "furni");
        }

        sb_add_string(sb, "_");
    }

    sb_add_string(sb, definition->sprite);

    if (special_sprite_id > 0) {
        sb_add_string(sb, "_");
        sb_add_int(sb, special_sprite_id);
    }

    char *text_key = strdup(sb->data);
    sb_cleanup(sb);

    return text_key;
}