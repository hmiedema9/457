#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <assert.h>

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
        strcpy(array[i],string);
        i++;
    }
    
    //printf("%s", array[0]);
    
    int w;
    for(w = 0; w < 6; w++){
        
        /* get the first token */
        strcpy(table_ip[w].prefix, strtok(array[w], " "));
        strcpy(table_ip[w].nexthop, strtok(NULL, " "));
        strcpy(table_ip[w].interface, strtok(NULL, "\n"));
        
    }
    
    printf("%s\n",table_ip[0].interface);
    
    free(line);
    fclose(fp);
    exit(EXIT_SUCCESS);
    
    return 0;
}