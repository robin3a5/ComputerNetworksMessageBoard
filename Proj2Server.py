import json
import socket
import threading
from datetime import datetime

# Define the server's address and port number
SERVER_ADDRESS = 'localhost'
SERVER_PORT = 5000
ID = 0
MessageID = 1
MessageDict = {}
ClientList = {}

# Define the commands that the server will respond to
COMMAND_CONNECT = 'connect'
COMMAND_POST = 'post'
COMMAND_USERS = 'users'
COMMAND_MESSAGE = 'message'
COMMAND_JOIN = 'join'
COMMAND_LEAVE = 'leave'

def handle_connect(args, client_socket):
    global ID
    global ClientList
    #args_list = list(args.values())
    args_list = list(args)
    #handle user already connected
    if args_list[0] in UserList:
        errorResponse = {'success': False, 'command': "connect", 'value': {'message': "Already connected"}}
        clientBytes = json.dumps(clientResponse).encode()
        byte_obj_with_newline = bytes(clientBytes + b"\n")
        client_socket.send(byte_obj_with_newline)
    else:    
        UserList.append(args_list[0])
        UserDict[str(ID)] = args_list[0]
        #send response to client
        clientResponse = {'success': True, 'command': "connect", 'value': {'ID': str(ID), 'Group1': "First Group", 'Group2': "Second Group", 'Group3': "Third Group", 'Group4': "Fourth Group", 'Group5': "Fifth Group"}}
        clientBytes = json.dumps(clientResponse).encode()
        ClientList[client_socket] = ID
        byte_obj_with_newline = bytes(clientBytes + b"\n")
        client_socket.send(byte_obj_with_newline)
        UserGroups[ID] = {}
        ID += 1


def handle_post(args, client_socket):
    global MessageID
    global MessageDict
    global ClientList
    #args_list = list(args.values())
    args_list = list(args)
    print("Sender: " + str(args_list[2]))
    print("Subject: " + str(args_list[1]))
    print("Message: " + str(args_list[0]))
    response = {'success': True, 'command': "post", 'value': {'MessageID': str(MessageID), 'Sender': str(UserDict[args_list[2]]), 'Date': str(datetime.now()), 'Subject': str(args_list[1])}}
    MessageDict[MessageID] = str(args_list[0])    
    MessageID = MessageID + 1
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    groupsToSend = UserGroups[int(args_list[2])]
    #client_socket.send(byte_obj_with_newline)
    for socket in ClientList:
        userId = ClientList[socket]
        if any(group in UserGroups[userId] for group in groupsToSend):
            socket.send(byte_obj_with_newline)


def handle_users(args, client_socket):
    args_list = list(args)
    userId = str(args_list[0])
    userGroups = UserGroups.get(userId, [])
    users_in_groups = [user for user in UserList if UserGroups.get(user, []) in userGroups]
    response = {'success': True, 'command': "users", 'value': {'users': users_in_groups}}
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    client_socket.send(byte_obj_with_newline)

def handle_message(args, client_socket):
    global MessageDict
    #args_list = list(args.values())
    args_list = list(args)
    key = int(args_list[0])
    response = {'success': True, 'command': "message", 'value': {'message': str(MessageDict[key])}}
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    client_socket.send(byte_obj_with_newline)

def handle_join(args, client_socket):
    global GROUPS
    global UserGroups
    #args_list = list(args.values())
    args_list = list(args)
    userId = int(args_list[0])
    groupId = str(args_list[1])
    groupName = str(args_list[2])
    print("UserID: " + str(userId))
    print("GroupID: " + str(groupId))
    print("GroupName: " + str(groupName))
    if groupId in GROUPS:
        UserGroups[userId].append(groupId)
        response = {'success': True, 'command': "join", 'value': {'message': "Joined group '" + groupId + "'"}}
        response_bytes = json.dumps(response).encode()
        byte_obj_with_newline = bytes(response_bytes + b"\n")
        client_socket.send(byte_obj_with_newline)
    elif groupName in GROUPS.values():
        groupId = tuple([key for key, value in GROUPS.items() if value == groupName])
        UserGroups[userId].append(groupId)
        response = {'success': True, 'command': "join", 'value': {'message': "Joined group '" + groupId + "'"}}
        response_bytes = json.dumps(response).encode()
        byte_obj_with_newline = bytes(response_bytes + b"\n")
        client_socket.send(byte_obj_with_newline)     
    else:
        response = {'success': False, 'command': "join", 'value': {'message': "Specified group does not exist"}}
        response_bytes = json.dumps(response).encode()
        byte_obj_with_newline = bytes(response_bytes + b"\n")
        client_socket.send(byte_obj_with_newline) 
        

def handle_leave(args, client_socket):
    global GROUPS
    global UserGroups
    #args_list = list(args.values())
    args_list = list(args)
    userId = args_list[0]
    groupId = args_list[1]
    groupName = args_list[2]
    if groupId in GROUPS:
        UserGroups[userId].remove(groupId)
        response = {'success': True, 'command': "leave", 'value': {'message': "Left group '" + groupId + "'"}}
        response_bytes = json.dumps(response).encode()
        byte_obj_with_newline = bytes(response_bytes + b"\n")
        client_socket.send(byte_obj_with_newline)
    elif groupName in GROUPS.values():
        groupId = tuple([key for key, value in GROUPS.items() if value == groupName])
        UserGroups[userId].remove(groupId)
        response = {'success': True, 'command': "leave", 'value': {'message': "Left group '" + groupId + "'"}}
        response_bytes = json.dumps(response).encode()
        byte_obj_with_newline = bytes(response_bytes + b"\n")
        client_socket.send(byte_obj_with_newline)     
    else:
        response = {'success': False, 'command': "leave", 'value': {'message': "Specified group does not exist"}}
        response_bytes = json.dumps(response).encode()
        byte_obj_with_newline = bytes(response_bytes + b"\n")
        client_socket.send(byte_obj_with_newline) 


def handle_command(jsonData, client_socket):
    # Parse the command into the command name and arguments
    commandType = jsonData['command']
    args = jsonData['value']
    # Get the function that handles this command
    handler = COMMAND_HANDLERS.get(commandType)

    # Call the handler function with the arguments
    if handler:
        handler(args, client_socket)
    else:
        response = {'success': False, 'value': {'message': "Unknown command"}}
        response_bytes = json.dumps(response).encode()
        byte_obj_with_newline = bytes(response_bytes + b"\n")
        client_socket.send(byte_obj_with_newline)


def handle_exit(jsonData):
    global ConnectedClients
    args = jsonData['args']
    #args_list = list(args.values())
    args_list = list(args)
    userKey = int(args_list[0])
    print(userKey)
    userName = UserDict.pop(str(userKey))
    UserList.remove(userName)
    response = {'success': True, 'command': "exit", 'value': {'message': "Client " + userName + " disconnected"}}
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    client_socket.send(byte_obj_with_newline)


def handle_client(client_socket):
    while True:
        # Receive a command from the client
        command_bytes = client_socket.recv(4096)
        if command_bytes:
            # command_body = command_bytes.decode('utf-8')
            command_body = remove_chars_until_brace(command_bytes)

            # Handle the command and send back a response
            jsonData = json.loads(command_body)
            if jsonData['command'] == 'exit':
                handle_exit(jsonData, client_socket)
                break;
            handle_command(jsonData, client_socket)
            #response_bytes = json.dumps(response).encode()
            #client_socket.send(response_bytes)
    # Clean up when the client disconnects
    client_socket.close()


def run_server():
    # Create a socket to listen for incoming connections
    UserDict = dict()
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((SERVER_ADDRESS, SERVER_PORT))
    server_socket.listen(5)

    # Wait for incoming connections and start a new thread for each one
    print('Server listening on ' + SERVER_ADDRESS + ':' + str(SERVER_PORT) + '...')
    while True:
        client_socket, client_address = server_socket.accept()
        thread = threading.Thread(target=handle_client, args=(client_socket,))
        thread.start()

    
def remove_chars_until_brace(s):
    if isinstance(s, (bytes, bytearray)):
        s = s.decode()
    brace_index = s.find('{')
    if brace_index == -1:
        # No brace found, return the original string
        return s
    else:
        # Return the substring starting at the first brace
        return s[brace_index:]

    
# Define a dictionary to map commands to functions
COMMAND_HANDLERS = {
    COMMAND_CONNECT: handle_connect, 
    COMMAND_POST: handle_post,
    COMMAND_USERS: handle_users,  
    COMMAND_MESSAGE: handle_message,
    COMMAND_JOIN: handle_join,
    COMMAND_LEAVE: handle_leave,
}

GROUPS = {
    'Group1': "First Group",
    'Group2': "Second Group",
    'Group3': "Third Group",
    'Group4': "Fourth Group",
    'Group5': "Fifth Group",
}
UserGroups = {}
UserList = []
UserDict = {}

if __name__ == '__main__':
    run_server()
