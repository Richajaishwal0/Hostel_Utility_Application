# Hostel Utility Application - Complete Build System
CC = gcc
JAVAC = javac
JAVA = java
CFLAGS = -Wall -Wextra

# Directories
MODULE1 = module1-socket
MODULE2 = module2-rmi
MODULE3 = module3-rest
MODULE4 = module4-p2p
MODULE5 = module5-shared-memory
UI = ui

.PHONY: all clean compile-java compile-c run-servers demo help

all: compile-java compile-c
	@echo "All modules compiled successfully!"
	@echo "Run 'make demo' to start the complete application"

# Compile Java modules
compile-java:
	@echo "Compiling Java modules..."
	cd $(MODULE1) && $(JAVAC) *.java
	cd $(MODULE2) && $(JAVAC) *.java
	cd $(MODULE3) && $(JAVAC) *.java
	cd $(MODULE4) && $(JAVAC) *.java

# Compile C modules
compile-c:
	@echo "Compiling C modules..."
	cd $(MODULE5) && $(CC) $(CFLAGS) -o feedback_server feedback_server.c semaphore.c
	cd $(MODULE5) && $(CC) $(CFLAGS) -o feedback_client feedback_client.c semaphore.c

# Run all servers
run-servers:
	@echo "Starting all backend servers..."
	@echo "Starting Socket Server (Module 1)..."
	cd $(MODULE1) && $(JAVA) ComplaintServer &
	@echo "Starting Socket Bridge (Module 1)..."
	cd $(MODULE1) && $(JAVA) ComplaintBridge &
	@echo "Starting RMI Server (Module 2)..."
	cd $(MODULE2) && $(JAVA) RoomInfoServer &
	@echo "Starting REST Server (Module 3)..."
	cd $(MODULE3) && $(JAVA) NoticeServer &
	@echo "Starting P2P Peers (Module 4)..."
	cd $(MODULE4) && $(JAVA) P2PPeer 9001 &
	cd $(MODULE4) && $(JAVA) P2PPeer 9002 &
	@echo "Starting Shared Memory Server (Module 5)..."
	cd $(MODULE5) && ./feedback_server &
	@echo "All servers started!"

# Complete demo
demo: all
	@echo "=========================================="
	@echo "  HOSTEL UTILITY APPLICATION - DEMO"
	@echo "=========================================="
	@echo ""
	@echo "Starting all backend services..."
	@make run-servers
	@echo ""
	@echo "Backend services are running!"
	@echo ""
	@echo "Open ui/index.html in your web browser to access the application"
	@echo ""
	@echo "Available modules:"
	@echo "1. Socket Programming - Complaint Management (Port 8080)"
	@echo "2. Java RMI - Room Information (Port 1099)"
	@echo "3. REST API - Notice Board (Port 8081)"
	@echo "4. P2P - Resource Sharing (Ports 9001, 9002)"
	@echo "5. Shared Memory - Mess Feedback"
	@echo ""
	@echo "Press Ctrl+C to stop all services"

# Test individual modules
test-socket:
	cd $(MODULE1) && $(JAVA) ComplaintServer &
	@echo "Socket server started on port 8080"

test-rmi:
	cd $(MODULE2) && $(JAVA) RoomInfoServer &
	@echo "RMI server started on port 1099"

test-rest:
	cd $(MODULE3) && $(JAVA) NoticeServer &
	@echo "REST server started on port 8081"

test-p2p:
	cd $(MODULE4) && $(JAVA) P2PPeer 9001 &
	cd $(MODULE4) && $(JAVA) P2PPeer 9002 &
	@echo "P2P peers started on ports 9001, 9002"

test-shared-memory:
	cd $(MODULE5) && ./feedback_server &
	@echo "Shared memory server started"
	@echo "Test with: cd $(MODULE5) && ./feedback_client good"

# Cleanup
clean:
	@echo "Cleaning compiled files..."
	find . -name "*.class" -delete
	cd $(MODULE5) && rm -f feedback_server feedback_client
	@echo "Cleanup complete!"

stop:
	@echo "Stopping all Java processes..."
	pkill -f "java.*Server" || true
	pkill -f "java.*P2PPeer" || true
	pkill -f "feedback_server" || true
	@echo "All processes stopped!"

# Help
help:
	@echo "Hostel Utility Application - Build System"
	@echo ""
	@echo "Available commands:"
	@echo "  make all           - Compile all modules"
	@echo "  make demo          - Start complete application demo"
	@echo "  make run-servers   - Start all backend servers"
	@echo "  make test-socket   - Test socket programming module"
	@echo "  make test-rmi      - Test RMI module"
	@echo "  make test-rest     - Test REST API module"
	@echo "  make test-p2p      - Test P2P module"
	@echo "  make test-shared-memory - Test shared memory module"
	@echo "  make clean         - Remove compiled files"
	@echo "  make stop          - Stop all running services"
	@echo "  make help          - Show this help message"