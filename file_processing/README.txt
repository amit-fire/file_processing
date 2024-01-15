Design

Option 1: Have a process that runs in the background and every X time, notes the offset of each line.
Offsets are stored in a separate file (each log file, has its own offset file).
This seems a bit too cumbersome. Definitely not something to implement for an assessment.
 
What is needed is something similar to the tail function in Unix/Linux

Option 2: call the tail command from the application
As mentioned in https://medium.com/@dotronglong/tail-in-java-8-9114a62eb88b, there are some issues with that.
I wanted to try it for myself, but don't have a unix/linux machine.

Option 3: implementing tail
I used the implementation mentioned in the blog above.
The code was modified to:
ignore empty lines
handle text search
[the changes have been docuemented in the code]

the service receives a request, validates the query parameters.
if the parameters are not valid, a friendly message is returned.
if they are valid, lines are fetched and sent to the consumer.

collecting all the lines and sending them together works up to a certain size.

ideally, the lines should be returned in batches.

this can be done in 4 ways
1. the consumer keeps track of the pagination.
2. the consumer provides a url, such that once a batch has been processed, it can be sent and the process runs until all required lines have been sent.
3. the consumer submits a post request to fetch the data.
if the query parameters are valid, the service responds with a unique id, which can later be used by the consumer to poll the service for the results.
if the query parameters are invalid, an error is returned.
4. websocket are meant for a real time messaging, but it's an option to explore. the socket can be kept open until all required lines have been sent, at which point the socket can be erminated.

Additionally, the data can be compressed (zipped) prior to sending it.
The 4 points above, are still relevant, even with compression.
Ideally, the service should expose the 2 options (either 2 end points or a query parameter) and let the consumer decide.

Stack: Java, Spring

Code (in folder file_processing)
There are 4 components.
FileProcessingController: this is where the request lands.
It's very light and logic resides in other classes.

FileProcessingService: determines if the required lines have been read.

Tail: the tail implementation.
text to search is very basic.
checks if the string contains the text to search.
it is case sensitive.
Ideally, regex should be supported and also allow the user to determine if sensitivity matters and if to match whole word.

RequestParamValidator: validates that the file exists and the the number of lines to read is a positive number.

Tests
Unit:
FileProcessingService has been tested (95% coverage). This is also testing Tail (90%).
The validations are too simple, hence has not been tested.
The controller class has no logic, hence has not been tested.

Manual:
Lines begin with a number, representing the line's number.
That allows to ensure that the required lines are returned.
The small file is part of the files.
The large file is over 3 GB. It's is not included.
However, included is code to generate file (GenerateFile - it's in the tests area).

Sample request: localhost:6000/app/entries?fileName=file.txt&numberOfEntries=100&stringToFind=a

Constants/Messages
Messages have not be externalized.
Constants such as batch size, buffer size, log dir are hard coded. for a production project, they would have been read from a config file.

Logging:
Some messages are logged via log4j2. log location: <file_processing_app_folder>/logs/app.log
Some messages are logged via System.out.println (would be logged via log4j2 in production).
Additional logs would be useful for a production code.

Running the application: see run application.docx

Output:
1. various png files
2. large_file_output.docx
