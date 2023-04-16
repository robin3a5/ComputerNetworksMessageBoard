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
COMMAND_GROUPPOST = 'grouppost'
COMMAND_GROUPUSERS = 'groupusers'
COMMAND_GROUPMESSAGE = 'groupmessage'
COMMAND_GROUPJOIN = 'groupjoin'
COMMAND_GROUPLEAVE = 'groupleave'

def handle_connect(args, client_socket):
    global ID
    global ClientList
    args_list = list(args)
    #handle user already connected
    if args_list[0] in UserList:
        errorResponse = {'success': False, 'command': "connect", 'value': {'message': "Already connected"}}
        clientBytes = json.dumps(errorResponse).encode()
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
        UserGroups[ID] = []
        handle_join(ID, client_socket)
        ID += 1


def handle_post(args, client_socket):
    global MessageID
    global MessageDict
    global ClientList
    args_list = list(args)
    response = {'success': True, 'command': "post", 'value': {'MessageID': str(MessageID), 'Sender': str(UserDict[args_list[2]]), 'Date': str(datetime.now()), 'Subject': str(args_list[1])}}
    MessageDict["Public"].append({MessageID : str(args_list[0])})    
    MessageID = MessageID + 1
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    for socket in ClientList:
        socket.send(byte_obj_with_newline)

def handle_groupPost(args, client_socket):
    global MessageID
    global MessageDict
    global ClientList
    args_list = list(args)
    groupID = args_list[3]
    response = {'success': True, 'command': "post", 'value': {'MessageID': str(MessageID), 'Sender': str(UserDict[args_list[2]]), 'Date': str(datetime.now()), 'Subject': str(args_list[1])}}
    MessageDict[groupID].append({MessageID : str(args_list[0])})
    MessageID += 1
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    for socket in ClientList:
        userId = ClientList[socket]  
        if groupID in UserGroups[userId]:
            socket.send(byte_obj_with_newline)    



def handle_users(args, client_socket):
    args_list = list(args)
    usersInGroup = []
    for userKey, userGroupList in UserGroups.items():
        if "Public" in userGroupList:
            usersInGroup.append(UserDict[str[userKey]])
    response = {'success': True, 'command': "users", 'value': {'users': usersInGroups}}
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    client_socket.send(byte_obj_with_newline)

def handle_groupUsers(args, client_socket):
    args_list = list(args)
    groupID = str(args_list[0])
    usersInGroup = []
    for userKey, userGroupList in UserGroups.items():
        if groupID in userGroupList:
            usersInGroup.append(UserDict[str[userKey]])
    response = {'success': True, 'command': "users", 'value': {'users': usersInGroups}}
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    client_socket.send(byte_obj_with_newline)


def handle_message(args, client_socket):
    global MessageDict
    args_list = list(args)
    messageID = int(args_list[0])
    groupID = str(args_list[1])
    responseMessage = ""
    for message in MessageDict["Public"]:
        messageKey = list(message.keys())[0]
        if messageKey == messageID:
            responseMessage = message[messageKey]
            break
    response = {'success': True, 'command': "message", 'value': {'message': responseMessage}}
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    client_socket.send(byte_obj_with_newline)

def handle_groupMessage(args, client_socket):
    global MessageDict
    args_list = list(args)
    messageID = int(args_list[0])
    groupID = str(args_list[1])
    responseMessage = ""
    for message in MessageDict[groupID]:
        messageKey = list(message.keys())[0]
        if messageKey == messageID:
            responseMessage = message[messageKey]
            break
    response = {'success': True, 'command': "message", 'value': {'message': responseMessage}}
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    client_socket.send(byte_obj_with_newline)

    
def handle_join(userId, client_socket):
    global GROUPS
    global UserGroups
    args_list = list(args)
    userName = UserDict[str(userId)]
    UserGroups[userId].append("Public")
    response = {'success': True, 'command': "join", 'value': {'message': "Joined public group"}}
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    client_socket.send(byte_obj_with_newline)
    for socket, user in ClientSockets:
        if socket != client_socket:
            response = {'success': True, 'command': "join", 'value': {'message': userName + " has joined the board"}}
            response_bytes = json.dumps(response).encode()
            byte_obj_with_newline = bytes(response_bytes + b"\n")
            socket.send(byte_obj_with_newline)


def handle_groupJoin(args, client_socket):
    global GROUPS
    global UserGroups
    args_list = list(args)
    userId = int(args_list[0])
    groupId = str(args_list[1])
    groupName = str(args_list[2])
    userName = UserDict[str(userId)]
    usersInGroup = []
    if groupId in GROUPS:
        UserGroups[userId].append(groupId)
        response = {'success': True, 'command': "join", 'value': {'message': "Joined group '" + groupId + "'"}}
        response_bytes = json.dumps(response).encode()
        byte_obj_with_newline = bytes(response_bytes + b"\n")
        client_socket.send(byte_obj_with_newline)
        for userKey, userGroupList in UserGroups.items():
            if groupId in userGroupList:
                usersInGroup.append(userKey)
        for socket, user in ClientSockets:
            if user in usersInGroup and socket != client_socket:
                response = {'success': True, 'command': "join", 'value': {'message': userName + " joined the group"}}
                response_bytes = json.dumps(response).encode()
                byte_obj_with_newline = bytes(response_bytes + b"\n")
                socket.send(byte_obj_with_newline)
    elif groupName in GROUPS.values():
        groupId = tuple([key for key, value in GROUPS.items() if value == groupName])
        UserGroups[userId].append(groupId)
        response = {'success': True, 'command': "join", 'value': {'message': "Joined group '" + groupId + "'"}}
        response_bytes = json.dumps(response).encode()
        byte_obj_with_newline = bytes(response_bytes + b"\n")
        client_socket.send(byte_obj_with_newline)
        for userKey, userGroupList in UserGroups.items():
            if groupId in userGroupList:
                usersInGroup.append(userKey)
        for socket, user in ClientSockets:
            if user in usersInGroup and socket != client_socket:
                response = {'success': True, 'command': "join", 'value': {'message': userName + " joined the group"}}
                response_bytes = json.dumps(response).encode()
                byte_obj_with_newline = bytes(response_bytes + b"\n")
                socket.send(byte_obj_with_newline)
    else:
        response = {'success': False, 'command': "join", 'value': {'message': "Specified group does not exist"}}
        response_bytes = json.dumps(response).encode()
        byte_obj_with_newline = bytes(response_bytes + b"\n")
        client_socket.send(byte_obj_with_newline)

def handle_leave(args, client_socket):
    global ClientList
    global UserGroups
    args_list = list(args)
    userId = args_list[0]
    UserGroups[userId].remove("Public")
    response = {'success': True, 'command': "leave", 'value': {'message': "Left the board"}}
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    client_socket.send(byte_obj_with_newline)
    ClientList.pop(client_socket)
    client_socket.close()
    handle_exit(userId)
    
def handle_groupLeave(args, client_socket):
    global GROUPS
    global UserGroups
    args_list = list(args)
    userId = args_list[0]
    groupId = args_list[1]
    groupName = args_list[2]
    userName = UserDict[str(userId)]
    usersInGroup = []
    if groupId in GROUPS:
        UserGroups[userId].remove(groupId)
        response = {'success': True, 'command': "leave", 'value': {'message': "Left group '" + groupId + "'"}}
        response_bytes = json.dumps(response).encode()
        byte_obj_with_newline = bytes(response_bytes + b"\n")
        client_socket.send(byte_obj_with_newline)
        for userKey, userGroupList in UserGroups.items():
            if groupId in userGroupList:
                usersInGroup.append(userKey)
        for socket, user in ClientSockets:
            if user in usersInGroup and socket != client_socket:
                response = {'success': True, 'command': "leave", 'value': {'message': userName + " left the group"}}
                response_bytes = json.dumps(response).encode()
                byte_obj_with_newline = bytes(response_bytes + b"\n")
                socket.send(byte_obj_with_newline)
    elif groupName in GROUPS.values():
        groupId = tuple([key for key, value in GROUPS.items() if value == groupName])
        UserGroups[userId].remove(groupId)
        response = {'success': True, 'command': "leave", 'value': {'message': "Left group '" + groupId + "'"}}
        response_bytes = json.dumps(response).encode()
        byte_obj_with_newline = bytes(response_bytes + b"\n")
        client_socket.send(byte_obj_with_newline)
        for userKey, userGroupList in UserGroups.items():
            if groupId in userGroupList:
                usersInGroup.append(userKey)
        for socket, user in ClientSockets:
            if user in usersInGroup and socket != client_socket:
                response = {'success': True, 'command': "leave", 'value': {'message': userName + " left the group"}}
                response_bytes = json.dumps(response).encode()
                byte_obj_with_newline = bytes(response_bytes + b"\n")
                socket.send(byte_obj_with_newline)
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


def handle_exit(userId):
    global UserList
    global UserDict
    userName = UserDict.pop(str(userId))
    UserList.remove(userName)
    response = {'success': True, 'command': "exit", 'value': {'message': "Client " + userName + " has left the board"}}
    response_bytes = json.dumps(response).encode()
    byte_obj_with_newline = bytes(response_bytes + b"\n")
    for socket in ClientList:
        socket.send(byte_obj_with_newline)


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
    global MessageDict
    MessageDict["Public"] = []
    MessageDict["Group1"] = []
    MessageDict["Group2"] = []
    MessageDict["Group3"] = []
    MessageDict["Group4"] = []
    MessageDict["Group5"] = []

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
    COMMAND_GROUPPOST: handle_groupPost,
    COMMAND_GROUPUSERS: handle_groupUsers,  
    COMMAND_GROUPMESSAGE: handle_groupMessage,
    COMMAND_GROUPJOIN: handle_groupJoin,
    COMMAND_GROUPLEAVE: handle_groupLeave,
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
