#ifndef CATALOGUE_PAGE_H
#define CATALOGUE_PAGE_H

typedef struct hashtable_s HashTable;

typedef struct catalogue_page_s {
    int id;
    int min_role;
    char *name_index;
    char *name;
    char *layout;
    char *image_headline;
    char *image_teasers;
    char *body;
    char *label_pick;
    char *label_extra_s;
    HashTable *label_extra;
} catalogue_page;

catalogue_page *catalogue_page_create(int, int, char*, char*, char*, char*, char*, char*, char*, char*, char*);

#endif