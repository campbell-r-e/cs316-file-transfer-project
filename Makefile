export BUILD_DIR = build
export SRC_DIR = src/filetransfer

all: server client

server: shared $(SRC_DIR)/server/Server.java
	echo "Build server"
	javac -d $(BUILD_DIR) -cp $(BUILD_DIR) $(SRC_DIR)/server/Server.java

client: shared client_classes $(SRC_DIR)/client/Client.java
	echo "Build client"
	javac -d $(BUILD_DIR) -cp $(BUILD_DIR) $(SRC_DIR)/client/Client.java

shared: shared_enums ft_message $(SRC_DIR)/shared/message/*.java
	echo "Build shared classes"
	javac -d $(BUILD_DIR) -cp $(BUILD_DIR) $(SRC_DIR)/shared/message/*.java

shared_enums: command_id error_code
	echo "Build shared enums"

ft_message: $(SRC_DIR)/shared/FTMessage.java
	javac -d $(BUILD_DIR) -cp $(BUILD_DIR) $(SRC_DIR)/shared/FTMessage.java

command_id: $(SRC_DIR)/shared/CommandID.java
	javac -d $(BUILD_DIR) -cp $(BUILD_DIR) $(SRC_DIR)/shared/CommandID.java

error_code: $(SRC_DIR)/shared/ErrorCode.java
	javac -d $(BUILD_DIR) -cp $(BUILD_DIR) $(SRC_DIR)/shared/ErrorCode.java

client_classes: $(SRC_DIR)/client/Command.java
	echo "Build client classes"
	javac -d $(BUILD_DIR) -cp $(BUILD_DIR) $(SRC_DIR)/client/Command.java