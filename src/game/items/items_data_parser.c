#include "stdio.h"
#include "stdlib.h"

#include "list.h"

#include "shared.h"

#include "item.h"
#include "items_data_parser.h"

/**
 * Parse the public room model items
 * @param model the model name to parse
 * @return the list of room items parsed
 */
List *item_parser_get_items(char *model) {
    char file_path[30];
    sprintf(file_path, "data/public_items/%s.dat", model);

    FILE *file = fopen(file_path, "r");

    if (!file) {
        return NULL;
    }

    List *items = NULL;
    list_new(&items);

    char *line = NULL;
    size_t len = 0;
    ssize_t read;

    int id = 0;

    while ((read = getline(&line, &len, file)) != -1) {
        char *str_x = get_argument(line, " ", 2);
        char *str_y = get_argument(line, " ", 3);
        char *str_z = get_argument(line, " ", 4);
        char *str_rotation = get_argument(line, " ", 5);
        char *public_custom_data = get_argument(line, " ", 6);

        item *room_item = item_create(
                id++,
                -1,
                -1,
                (int) strtol(str_x, NULL, 10),
                (int) strtol(str_y, NULL, 10),
                (int) strtol(str_z, NULL, 10),
                (int) strtol(str_rotation, NULL, 10),
                get_argument(line, " ", 0)
        );


        room_item->definition = item_definition_create_blank();
        room_item->definition->sprite = get_argument(line, " ", 1);
        room_item->definition->behaviour->isPublicSpaceObject = true; // All items loaded this way are considered public space objects.

        // Filter unwanted characters
        filter_vulnerable_characters(&room_item->definition->sprite, true);

        if (public_custom_data != NULL) {
            filter_vulnerable_characters(&public_custom_data, true);

            if (public_custom_data[0] == '2') {
                room_item->definition->behaviour->has_extra_parameter = true;
                free(public_custom_data);
            } else {
                room_item->current_program = public_custom_data;
            }
        }

        if (strstr(room_item->definition->sprite, "chair") != NULL
            || strstr(room_item->definition->sprite, "bench") != NULL
            || strstr(room_item->definition->sprite, "seat") != NULL
            || strstr(room_item->definition->sprite, "stool") != NULL
            || strstr(room_item->definition->sprite, "sofa") != NULL
            || strcmp(room_item->definition->sprite, "l") == 0
            || strcmp(room_item->definition->sprite, "m") == 0
            || strcmp(room_item->definition->sprite, "k") == 0
            || strcmp(room_item->definition->sprite, "shift1") == 0) {
            room_item->definition->behaviour->can_sit_on_top = true;
        } else {
            room_item->definition->behaviour->can_sit_on_top = false;
            room_item->definition->behaviour->can_stand_on_top = false;
        }

        if (strcmp(room_item->definition->sprite, "poolEnter") == 0
            || strcmp(room_item->definition->sprite, "poolExit") == 0
            || strcmp(room_item->definition->sprite, "poolLift") == 0
            || strcmp(room_item->definition->sprite, "poolBooth") == 0
            || strcmp(room_item->definition->sprite, "queue_tile2") == 0) {
            room_item->definition->behaviour->can_sit_on_top = false;
            room_item->definition->behaviour->can_stand_on_top = true;
        }

        if (strcmp(room_item->definition->sprite, "queue_tile2") == 0) {
            free(room_item->custom_data);
            room_item->custom_data = strdup("2");
        }

        list_add(items, room_item);

        free(str_x);
        free(str_y);
        free(str_z);
        free(str_rotation);
    }

    free(line);
    fclose(file);

    /*if (strcmp(model, "cafe_ole") == 0) {
        file = fopen(file_path, "w+");

        for (int i = 0; i < list_size(items); i++) {
            item *room_item;
            list_get_at(items, i, (void*)&room_item);

            char custom_content[10];

            if (room_item->is_table) {
                strcpy(custom_content, " 2");
            } else {
                strcpy(custom_content, "");
            }

            char buf[100];
            if (strlen(custom_content) > 0) {
                sprintf(buf, "%s %s %i %i %i %i%s\n", room_item->custom_data, room_item->definition->sprite, room_item->x,
                        room_item->y, (int) room_item->z, room_item->rotation, custom_content);
            } else {
                sprintf(buf, "%s %s %i %i %i %i\n", room_item->custom_data, room_item->definition->sprite, room_item->x,
                        room_item->y, (int) room_item->z, room_item->rotation);
            }
            fputs(buf, file);
        }
        
        fclose(file);
    }*/

    return items;
}

