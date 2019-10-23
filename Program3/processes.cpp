#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <sys/wait.h>
#include <unistd.h>

using namespace std;

// This program is designed to simulate the linux command 'ps -A | grep argv[1] | wc -l'.
// In other words, it determines the number of instances of a running process argv[1].
int main(int argc, char *argv[]) {

    int fd1[2], fd2[2]; // Two arrays containing read and write file descriptors

    // Look for an invalid number of command line arguments.
    if (argc != 2) {
        perror("Invalid Number of Arguments");
    }

    // Make sure pipes are created successfully.
    if (pipe(fd1) < 0 || pipe(fd2) < 0) {
        perror("Piping Error Encountered");
    }

    // Create a child process and switch on its process ID.
    switch (fork()) {
        case -1:
            perror("Forking Error Encountered");
        case 0:
            // Close unused write file descriptors.
            close(fd1[0]);
            close(fd1[1]);
            close(fd2[1]);

            // Duplicate and close used read file descriptor.
            dup2(fd2[0], 0);
            close(fd2[0]);

            // Child "wc -l"
            execlp("wc", "wc", "-l", NULL);
        default:
            // Create a grand-child process and switch on its process ID.
            switch (fork()) {
                case -1:
                    perror("Forking Error Encountered");
                case 0:
                    // Close unused read and write file descriptors.
                    close(fd1[1]);
                    close(fd2[0]);

                    // Duplicate used read and write file descriptors.
                    dup2(fd1[0], 0);
                    dup2(fd2[1], 1);

                    // Close used read and write file descriptors.
                    close(fd1[0]);
                    close(fd2[1]);

                    // Grand-child "grep {argv[1]}"
                    execlp("grep", "grep", argv[1], NULL);
                default:
                    // Create a great-grand-child process and switch on its process ID.
                    switch (fork()) {
                        case -1:
                            perror("Forking Error Encountered");
                        case 0:
                            // Close unused read and write file descriptors.
                            close(fd1[0]);
                            close(fd2[0]);
                            close(fd2[1]);

                            // Duplicate used write file descriptor.
                            dup2(fd1[1], 1);
                            close(fd1[1]);

                            // Great-grand-child "ps -A"
                            execlp("ps", "ps", "-A", NULL);
                        default:
                            // Wait for children and exit successfully.
                            wait(NULL);
                            exit(EXIT_SUCCESS);
                    }
            }
    }
}