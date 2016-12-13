#include <stdio.h>
#include <string.h>
#include <stdlib.h>

int i = 0;
char s;
char array[6][50];
char string[50];

struct tableentry{
    char prefix[19];
    char nexthop[16];
    char interface[8];
};

int main()
{
    char ch, file_name[25];
    FILE *fp;
    struct tableentry table_ip[6];
    
    printf("Enter the name of file you wish to see\n");
    gets(file_name);
    
    fp = fopen(file_name,"r"); // read mode
    
    if( fp == NULL )
    {
        perror("Error while opening the file.\n");
        exit(EXIT_FAILURE);
    }
    
    char *line = NULL;
    size_t len = 0;
    ssize_t read;
    
    while ((read = getline(&line, &len, fp)) != -1) {
        printf("Retrieved line of length %zu :\n", read);
        printf("%s", line);
        strcpy(string, line);
        string[read] = '\0';
        strcpy(table_ip[i].prefix, strtok(string, " "));
        strcpy(table_ip[i].nexthop, strtok(NULL, " "));
        strcpy(table_ip[i].interface, strtok(NULL, "\n"));
        i++;
    }
    
    printf("%s\n",table_ip[0].interface);
    
    free(line);
    fclose(fp);
    exit(EXIT_SUCCESS);
    
    return 0;
}