#ifndef ITEM_QUERY_H
#define ITEM_QUERY_H

typedef struct item_s item;
typedef struct list_s List;

List *item_query_get_inventory(int user_id);
int item_query_create(int user_id, int room_id, int definition_id, int x, int y, double z, int rotation, char *custom_data);
void item_query_save(item *item);

#endif