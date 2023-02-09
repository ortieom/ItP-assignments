#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include <stdlib.h>

#define NAME_STR_LEN 25
#define MAX_PLAYERS 100

int superCnt = 0;  // global counter for super player

struct Player {
    char name[NAME_STR_LEN];
    int team, power;
    bool visibility;
};

struct Team {
    char wizard[NAME_STR_LEN];
    int players_cnt, score;
    struct Player members[MAX_PLAYERS];
};

void ReportError (FILE* input, FILE* output);  // writes error message into output.txt & closes files

bool StringsEqual (char* st1, char* st2);  // checks st1 == st2

bool IsStringValidNumber (char* line);  // validates string that is supposed to contain only one number

int CheckAndConvertNumber (char* line, int min_value, int max_value);  // checks number in string and converts to int

bool IsStringValidName (char* line, int teamsCnt, struct Team* teams);  // validates name

struct Player* LinSearchPlayer (char* name, int teamsCnt, struct Team* teams);  // returns pointer to player by name

// actions for players
void FlipVisibilityAction (struct Player* player1, FILE* output);
void AttackAction (struct Player* player1, struct Player* player2, struct Team* teams, FILE* output);
void HealAction (struct Player* player1, struct Player* player2, struct Team* teams, FILE* output);
void SuperAction (struct Player* player1, struct Player* player2, FILE* output);

int main() {
    // declaring input & output files
    FILE* input = fopen("input.txt", "r");
    FILE* output = fopen("output.txt", "w");

    char inputBuffer[100], name[NAME_STR_LEN];

    // input N
    fgets(inputBuffer, 10, input);
    int N = CheckAndConvertNumber(inputBuffer, 1, 10);
    if (N == -1) {
        ReportError(input, output);
        return 0;
    }

    struct Team teams[N];  // array of teams

    // magicians
    for (int i = 0; i < N; i++) {
        // setting default values for team
        teams[i].players_cnt = 0;
        teams[i].score = 0;

        fgets(name, NAME_STR_LEN, input);
        name[strlen(name) - 1] = '\0';  // removing \n
        if (!IsStringValidName(name, i, teams)) {
            // invalid name
            ReportError(input, output);
            return 0;
        }

        for (int j = 0; j < NAME_STR_LEN; j++) {  // copying wizard's name
            teams[i].wizard[j] = name[j];
        }
    }

    // input M
    fgets(inputBuffer, 10, input);
    int M = CheckAndConvertNumber(inputBuffer, N, 100);
    if (M == -1) {
        ReportError(input, output);
        return 0;
    }

    // players
    for (int i = 0; i < M; i++) {
        // name
        fgets(name, NAME_STR_LEN, input);
        name[strlen(name) - 1] = '\0';  // removing \n
        if (!IsStringValidName(name, N, teams)) {
            // invalid name
            ReportError(input, output);
            return 0;
        }

        // team number
        fgets(inputBuffer, 10, input);
        int teamNo = CheckAndConvertNumber(inputBuffer, 0, N - 1);
        if (teamNo == -1) {
            ReportError(input, output);
            return 0;
        }

        // power
        fgets(inputBuffer, 10, input);
        int power = CheckAndConvertNumber(inputBuffer, 0, 1000);
        if (power == -1) {
            ReportError(input, output);
            return 0;
        }

        // visibility
        char visibility[10];
        fgets(visibility, 10, input);
        if (!(StringsEqual(visibility, "True\n") || StringsEqual(visibility, "False\n"))) {
            ReportError(input, output);
            return 0;
        }

        // adding player to corresponding team
        int playerId = teams[teamNo].players_cnt;
        for (int j = 0; j < strlen(name); j++) {  // copying name
            teams[teamNo].members[playerId].name[j] = name[j];
            teams[teamNo].members[playerId].name[j + 1] = '\0';
        }
        teams[teamNo].members[playerId].team = teamNo;
        teams[teamNo].members[playerId].power = power;
        teams[teamNo].members[playerId].visibility = StringsEqual(visibility, "True\n");
        // updating team statistics
        teams[teamNo].players_cnt++;
        teams[teamNo].score += power;
    }

    int commandCnt = 0;
    char commandLine[55];
    while (fgets(commandLine, 55, input)) {
        if (++commandCnt > 1000) {  // too many actions
            ReportError(input, output);
            return 0;
        }
        commandLine[strlen(commandLine) - 1] = '\0';  // removing '\n' at the end
        char command[16] = "\0", arg1[21] = "\0", arg2[21] = "\0";

        // splitting command in parts
        int spaceCnt = 0, offset = 0;
        for (int i = 0; i < strlen(commandLine); i++) {
            if (commandLine[i] == ' ') {
                offset = i + 1;  // start of the new word
                spaceCnt++;
                if (spaceCnt > 2) {  // too many arguments
                    ReportError(input, output);
                    return 0;
                }
            } else {
                switch (spaceCnt) {
                    case 0:  // command
                        command[i] = commandLine[i];
                        command[i + 1] = '\0';
                        break;
                    case 1:  // 1st player's name
                        arg1[i - offset] = commandLine[i];
                        arg1[i - offset + 1] = '\0';
                        break;
                    case 2:  // 2nd player's name
                        arg2[i - offset] = commandLine[i];
                        arg2[i - offset + 1] = '\0';
                        break;
                    default:  // something unexpected
                        ReportError(input, output);
                        return 0;
                }
            }
        }

        // checking command and arguments
            if (strlen(command) == 0 || strlen(arg1) == 0) {
            // if command or first argument is missing
            ReportError(input, output);
            return 0;
        }
        if (StringsEqual(command, "flip_visibility")) {  // special case with "flip_visibility" command
            if (strlen(arg2) != 0) {  // extra 2nd argument
                ReportError(input, output);
                return 0;
            }
        } else if (strlen(arg2) == 0) {  // missing 2nd argument
            ReportError(input, output);
            return 0;
        }

        // obtaining pointer to 1st player by name
        struct Player *player1 = LinSearchPlayer(arg1, N, teams);
        if (player1->name[0] == '\0') {  // player does not exist
            ReportError(input, output);
            return 0;
        }

        // evaluating actions
        if (StringsEqual(command, "flip_visibility")) {
            FlipVisibilityAction(player1, output);
        } else {
            // obtaining pointer to 2nd player by name
            struct Player *player2 = LinSearchPlayer(arg2, N, teams);
            if (player2->name[0] == '\0') {  // player does not exist
                ReportError(input, output);
                return 0;
            }

            if (StringsEqual(command, "attack")) {
                AttackAction(player1, player2, teams, output);
            } else if (StringsEqual(command, "heal")) {
                HealAction(player1, player2, teams, output);
            } else if (StringsEqual(command, "super")) {
                SuperAction(player1, player2, output);
            } else {  // unknown action
                ReportError(input, output);
                return 0;
            }

            // if one of the players gained power > 1000
            if (player1->power > 1000) {
                teams[player1->team].score -= player1->power - 1000;
                player1->power = 1000;
            }
            if (player2->power > 1000) {
                teams[player2->team].score -= player2->power - 1000;
                player2->power = 1000;
            }
        }
    }

    // preparing answer
    int max_score = -1, winnerTeam;
    bool tie = false;
    for (int i = 0; i < N; i++) {
        if (teams[i].score > max_score) {
            tie = false;
            max_score = teams[i].score;
            winnerTeam = i;
        } else if (teams[i].score == max_score) {
            tie = true;
        }
    }

    // answer output
    if (tie) {
        fprintf(output, "It's a tie\n");
    } else {
        fprintf(output, "The chosen wizard is %s\n", teams[winnerTeam].wizard);
    }

    fclose(input);
    fclose(output);
    return 0;
}

void ReportError (FILE* input, FILE* output) {
    // writes error message into output.txt & closes files
    fclose(output);
    output = fopen("output.txt", "w");
    fprintf(output, "Invalid inputs\n");
    fclose(output);
    fclose(input);
}

bool StringsEqual(char* st1, char* st2) {
    // checks st1 == st2
    unsigned int l1 = strlen(st1), l2 = strlen(st2);
    if (l1 != l2) {  // by length
        return false;
    }
    for (int i = 0; i < l1; i++) {  // by symbols
        if (st1[i] != st2[i]) {
            return false;
        }
    }
    return true;
}

bool IsStringValidNumber (char* line) {
    // validates string that is supposed to contain only one number
    unsigned int len = strlen(line) - 1;
    line[len] = '\0';
    for (int i = 0; i < len; i++) {
        if (!(line[i] >= '0' && line[i] <= '9')) {
            return false;
        }
    }
    return true;
}

int CheckAndConvertNumber (char* line, int min_value, int max_value) {
    // checks number in string and converts to int
    if (!IsStringValidNumber(line)) {
        return -1;
    }
    int n = atoi(line);
    if (!(min_value <= n && n <= max_value)) {  // out of boundaries
        return -1;
    }
    return n;
}

bool IsStringValidName (char* line, int teamsCnt, struct Team* teams) {
    // validates name
    unsigned int len = strlen(line);

    if (!(2 <= len && len <= 20)) {  // invalid length
        return false;
    }
    if (!(line[0] >= 'A' && line[0] <= 'Z')) {  // first symbol is not an uppercase letter
        return false;
    }
    for (int i = 1; i < len; i++) {
        if (!(line[i] >= 'a' && line[i] <= 'z') && !(line[i] >= 'A' && line[i] <= 'Z')) {
            // symbol is not an english letter
            return false;
        }
    }

    // check for uniqueness
    for (int i = 0; i < teamsCnt; i++) {
        if (StringsEqual(teams[i].wizard, line)) {  // equal to one of the wizards
            return false;
        }
        for (int j = 0; j < teams[i].players_cnt; j++) {
            if (StringsEqual(teams[i].members[j].name, line)) {  // equal to one of the players
                return false;
            }
        }
    }

    return true;
}

struct Player emptyPlayer = {.name = ""};  // dummy structure with empty name
struct Player * LinSearchPlayer (char* name, int teamsCnt, struct Team* teams) {
    /* linear search, returns pointer to player by name
     * if not found, returns pointer to a dummy structure with empty name */
    for (int i = 0; i < teamsCnt; i++) {
        for (int j = 0; j < teams[i].players_cnt; j++) {
            if (StringsEqual(teams[i].members[j].name, name)) {
                return &teams[i].members[j];
            }
        }
    }
    return &emptyPlayer;
}

// "flip_visibility" action
void FlipVisibilityAction (struct Player* player1, FILE* output) {
    if (player1->power == 0) {
        fprintf(output, "This player is frozen\n");
    } else {
        player1->visibility = !player1->visibility;
    }
}

// "attack" action
void AttackAction (struct Player* player1, struct Player* player2, struct Team* teams, FILE* output) {
    if (player1->visibility == false) {  // visibility check
        fprintf(output, "This player can't play\n");
        return;
    }
    if (player1->power == 0) {  // frozen check
        fprintf(output, "This player is frozen\n");
        return;
    }

    // action steps
    if (player2->visibility == false) {
        teams[player1->team].score -= player1->power;
        player1->power = 0;
    } else if (player1->power > player2->power) {
        teams[player1->team].score += player1->power - player2->power;
        player1->power += player1->power - player2->power;
        teams[player2->team].score -= player2->power;
        player2->power = 0;
    } else if (player1->power < player2->power) {
        teams[player2->team].score += player2->power - player1->power;
        player2->power += player2->power - player1->power;
        teams[player1->team].score -= player1->power;
        player1->power = 0;
    } else {
        teams[player1->team].score -= player1->power;
        player1->power = 0;
        teams[player2->team].score -= player2->power;
        player2->power = 0;
    }
}

// "heal" action
void HealAction (struct Player* player1, struct Player* player2, struct Team* teams, FILE* output) {
    if (player1->visibility == false) {  // visibility check
        fprintf(output, "This player can't play\n");
        return;
    }
    if (player1->power == 0) {  // frozen check
        fprintf(output, "This player is frozen\n");
        return;
    }
    if (player1->team != player2->team) {  // same team check
        fprintf(output, "Both players should be from the same team\n");
        return;
    }
    if (StringsEqual(player1->name, player2->name)) {  // same player check
        fprintf(output, "The player cannot heal itself\n");
        return;
    }

    // action steps
    if (player1->power % 2 != 0) {
        // if power is odd, then total score will increase by 1 due to ceiling numbers
        teams[player1->team].score++;
    }
    int shared_power = (player1->power + 1) / 2;  // how much power should each player have after
    player1->power = shared_power;
    player2->power += shared_power;
}

// "super" action
void SuperAction (struct Player* player1, struct Player* player2, FILE* output) {
    if (player1->visibility == false) {  // visibility check
        fprintf(output, "This player can't play\n");
        return;
    }
    if (player1->power == 0) {  // frozen check
        fprintf(output, "This player is frozen\n");
        return;
    }
    if (player1->team != player2->team) {  // same team check
        fprintf(output, "Both players should be from the same team\n");
        return;
    }
    if (StringsEqual(player1->name, player2->name)) {  // same player check
        fprintf(output, "The player cannot do super action with itself\n");
        return;
    }

    // action steps
    char new_name[5];
    sprintf(new_name, "S_%d", superCnt);
    for (int i = 0; i < 5; i++) {  // writing new name
        player1->name[i] = new_name[i];
    }
    player1->power += player2->power;
    player2->name[0] = '\0';  // hiding 2nd player
    superCnt++;
}
