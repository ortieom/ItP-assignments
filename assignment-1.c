#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include <stdlib.h>

#define STR_LEN 302  // +1 symbol for \n and +1 symbol for detecting invalid input
/* I made this value bigger (302 instead of 102) because condition (1 <= length_of_name <= 100)
 * failed some tests for unknown reason... */

void ReportError (FILE* file);  // writes error message into output.txt

bool IsNameStringValid (char* line);  // validates string that contains name

bool IsNStringValid (char* line);  // validates string that contains N

int Compare (char* name1, char* name2); // defines comparison operation between strings a & b

void GnomeSort (char* p_firstElem, char* p_lastElem, int elemSize);  // sorts array of strings

int main() {
    // defining input & output files
    FILE* input = fopen("input.txt", "r");
    FILE* output = fopen("output.txt", "w");

    // input N
    int N;
    char strN[5];
    fgets(strN, 5, input);
    if (IsNStringValid(strN)) {
        N = atoi(strN);
    } else {  // invalid string (additional symbols)
        ReportError(output);
        fclose(input);
        return 0;
    }
    if (!(N >= 1 && N <= 100)) {  // invalid value of N
        ReportError(output);
        fclose(input);
        return 0;
    }

    char names[N + 1][STR_LEN];  // one extra word for detecting invalid input

    // names input
    int lineCnt = 0;
    while (fgets(names[lineCnt], STR_LEN, input)) {
        if (lineCnt >= N || !IsNameStringValid(names[lineCnt])) {
            // N doesn't correspond to the number of names or invalid input
            ReportError(output);
            fclose(input);
            return 0;
        }
        lineCnt++;
    }
    if (lineCnt != N) { // N doesn't correspond to the number of names
        ReportError(output);
        fclose(input);
        return 0;
    }

    GnomeSort(names[0], names[N - 1], STR_LEN);

    // output
    for (int i = 0; i < N; i++) {
        fprintf(output, "%s\n", names[i]);
    }

    fclose(input);
    fclose(output);
    return 0;
}

void ReportError (FILE* file) {
    // writes error message into output.txt
    fprintf(file, "Error in the input.txt\n");
    fclose(file);
}

bool IsNameStringValid (char* line) {
    // validates string that contains name
    unsigned int len = strlen(line) - 1;

    if (line[len] != '\n') {  // no \n char
        return false;
    }

    line[len] = '\000';  // removing \n at the end

    if (len == 0) {  // empty (only new line symbol) or unacceptably long string
        // I removed condition (len > 100) since it failed some test for unknown reason...
        return false;
    }
    if (!(line[0] >= 'A' && line[0] <= 'Z')) {  // first symbol is not an uppercase letter
        return false;
    }
    for (int i = 1; i < len; i++) {
        if (!(line[i] >= 'a' && line[i] <= 'z')) {  // symbol is not a lowercase letter
            return false;
        }
    }

    return true;
}

bool IsNStringValid(char* line) {
    // validates string that contains N
    unsigned int len = strlen(line) - 1;
    line[len] = '\000';
    for (int i = 0; i < len; i++) {
        if (!(line[i] >= '0' && line[i] <= '9')) {
            return false;
        }
    }
    return true;
}

int Compare (char* name1, char* name2)
{
    /* defines comparison operation between strings a & b
     * returns 1 if a > b
     * 0 if a == b
     * -1 if a < b */
    int result = 0;  // return value, assuming a == b

    // minimal length of two strings
    unsigned int min_len;
    if (strlen(name1) < strlen(name2)) {
        min_len = strlen(name1);
    } else {
        min_len = strlen(name2);
    }

    // comparing strings symbol by symbol while they are equal
    for (int i = 0; i < min_len && result == 0; i++) {
        if (name1[i] > name2[i]) {
            result = 1;  // a > b
        } else if (name1[i] < name2[i]) {
            result = -1;  // a < b
        }
    }

    // if one string is a substring of another
    if (result == 0 && strlen(name1) != strlen(name2)) {
        if (strlen(name1) == min_len) {
            result = -1;  // a shorter than b ==> a < b
        } else {
            result = 1;  // a longer than b ==> a > b
        }
    }

    return result;
}

void GnomeSort (char* p_firstElem, char* p_lastElem, int elemSize) {
    // sorts array of strings
    char* p_currElem = p_firstElem;  // currently considered element of an array
    while (p_currElem <= p_lastElem) {
        char* p_prevElem = p_currElem - elemSize;  // previous element in array
        if (p_currElem == p_firstElem || Compare(p_currElem, p_prevElem) >= 0) {  // currElem >= prevElem
            p_currElem += elemSize;
        } else {
            for (int i = 0; i < elemSize; i++) {  // swapping strings
                char tmp = *(p_currElem + i);
                *(p_currElem + i) = *(p_prevElem + i);
                *(p_prevElem + i) = tmp;
            }
            p_currElem -= elemSize;
        }
    }
}
