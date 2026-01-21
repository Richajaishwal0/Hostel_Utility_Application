# Hostel Utility Application - Distributed Systems Project

## Overview

A comprehensive hostel management system demonstrating **all 5 distributed communication paradigms** with functional web UI and optimized **in-memory data storage**.

## Project Architecture

### **Module Implementation**

### **Module 1: Socket Programming - Complaint Management System**
- **Technology**: Java Socket Programming with Multi-threading
- **Functionality**: Students submit hostel complaints via web UI
- **Ports**: 8080 (Socket Server), 8082 (HTTP Bridge)
- **Files**: `ComplaintServer.java`, `ComplaintBridge.java`, `ComplaintWebClient.java`

### **Module 2: Java RMI - Room Information Service**
- **Technology**: Java Remote Method Invocation
- **Functionality**: Students search room details and warden information
- **Ports**: 1099 (RMI Registry), 8083 (HTTP Bridge)
- **Files**: `RoomInfoServer.java`, `RoomInfoService.java`, `RoomInfoBridge.java`

### **Module 3: REST API - Notice Board System**
- **Technology**: HTTP REST API using Java HttpServer
- **Functionality**: Admin posts notices, students view announcements
- **Ports**: 8081
- **Files**: `NoticeServer.java`

### **Module 4: P2P - Resource Sharing System**
- **Technology**: Peer-to-Peer networking with decentralized architecture
- **Functionality**: Students share academic resources directly
- **Ports**: 9001, 9002 (P2P Peers), 8085 (HTTP Bridge)
- **Files**: `P2PPeer.java`, `P2PBridge.java`

### **Module 5: Shared Memory - Mess Feedback Counter**
- **Technology**: Java Shared Memory Simulation with Synchronization
- **Functionality**: Live feedback counting with process synchronization
- **Ports**: 8084
- **Files**: `SharedMemoryFeedbackServer.java`

## Quick Start Guide

### **Prerequisites**
- Java JDK 8+ (for all modules)
- Modern web browser
- Windows/Linux/macOS

### **One-Command Startup**

```cmd
start_all.bat
```

This single command will:
1. **Compile** all Java modules automatically
2. **Start** all 9 servers in separate windows
3. **Display** server status and ports

### **Server Ports Summary**

| Service | Port | Description |
|---------|------|-------------|
| Socket Server | 8080 | Complaint management backend |
| Socket Bridge | 8082 | HTTP bridge for complaints |
| RMI Server | 1099 | Room information RMI registry |
| RMI Bridge | 8083 | HTTP bridge for room info |
| REST Server | 8081 | Notice board API |
| P2P Peer 1 | 9001 | File sharing peer |
| P2P Peer 2 | 9002 | File sharing peer |
| P2P Bridge | 8085 | HTTP bridge for P2P |
| Shared Memory Server | 8084 | Feedback counter |

### **Access the Application**

After starting servers, open the web interface:
```cmd
# Open in your default browser
start ui\index.html
```

### **Manual Startup (Alternative)**

If you prefer to run servers individually:

```cmd
# Navigate to project directory
cd "C:\path\to\hostel-utility-app"

# Compile all modules
javac module1-socket\*.java
javac module2-rmi\*.java
javac module3-rest\*.java
javac module4-p2p\*.java
javac module5-shared-memory\*.java

# Start servers (each in separate terminal)
start cmd /k "cd module1-socket && java ComplaintServer"
start cmd /k "cd module1-socket && java ComplaintBridge"
start cmd /k "cd module2-rmi && java RoomInfoServer"
start cmd /k "cd module2-rmi && java RoomInfoBridge"
start cmd /k "cd module3-rest && java NoticeServer"
start cmd /k "cd module4-p2p && java P2PPeer 9001"
start cmd /k "cd module4-p2p && java P2PPeer 9002"
start cmd /k "cd module4-p2p && java P2PBridge"
start cmd /k "cd module5-shared-memory && java SharedMemoryFeedbackServer"
```

## User Interface Features

### **Web-Based Dashboard**
- **File**: `ui/index.html`
- **Technology**: HTML5, CSS3, JavaScript
- **Features**: Module-based navigation, real-time updates, AJAX communication

### **Module-Specific UI**
1. **Complaints**: Submit and view complaint history
2. **Room Info**: Search rooms via RMI calls
3. **Notices**: Admin panel and student notice board
4. **P2P Resources**: File upload, search, and download
5. **Feedback**: Live voting with real-time counters

## Technical Architecture

### **Distributed Systems Concepts Demonstrated**
1. **Client-Server Architecture** (Socket, RMI, REST)
2. **Peer-to-Peer Communication** (P2P File Sharing)
3. **Inter-Process Communication** (Shared Memory)
4. **Concurrency & Synchronization** (Thread pools, AtomicInteger, ReentrantLock)
5. **Network Protocols** (TCP Sockets, HTTP, RMI Protocol)

### **Data Storage Strategy**
- **In-Memory Storage**: All modules use memory-based storage for optimal performance
- **Thread-Safe Collections**: `Collections.synchronizedList()`, `ConcurrentHashMap`, `AtomicInteger`
- **Persistence**: Complaint module includes file-based backup (`complaints.dat`)

## Troubleshooting

### **Common Issues**

**Port Conflicts:**
- Ensure no other applications are using ports 8080-8085, 1099, 9001-9002
- Check Windows Firewall settings

**Compilation Errors:**
- Verify Java JDK 8+ is installed: `java -version`
- Ensure JAVA_HOME is set correctly

**Server Connection Issues:**
- Wait 10-15 seconds after starting servers before accessing UI
- Check if all server windows opened successfully
- Restart servers if any failed to start

**Frontend Not Loading:**
- Open `ui/index.html` directly in browser
- Check browser console for JavaScript errors
- Ensure all servers are running before testing modules

### **Stopping All Servers**

To stop all running servers:
```cmd
# Kill all Java processes (Windows)
taskkill /f /im java.exe

# Or close each server window individually
```

## Project Structure

```
hostel-utility-app/
├── module1-socket/          # Socket Programming
│   ├── ComplaintServer.java
│   ├── ComplaintBridge.java
│   └── ComplaintWebClient.java
├── module2-rmi/             # Java RMI
│   ├── RoomInfoServer.java
│   ├── RoomInfoService.java
│   └── RoomInfoBridge.java
├── module3-rest/            # REST API
│   └── NoticeServer.java
├── module4-p2p/             # P2P Networking
│   ├── P2PPeer.java
│   ├── P2PBridge.java
│   ├── shared/              # Shared files directory
│   └── downloads/           # Downloaded files directory
├── module5-shared-memory/   # Shared Memory IPC
│   └── SharedMemoryFeedbackServer.java
├── ui/                      # Web Interface
│   ├── index.html
│   ├── script.js
│   └── style.css
├── start_all.bat           # Main startup script
├── Makefile               # Build automation
└── README.md              # This file
```

## Learning Outcomes

### **Distributed Systems Mastery**
- ✅ **Socket Programming**: Multi-client handling, protocol design
- ✅ **RMI**: Remote objects, stub-skeleton architecture
- ✅ **REST APIs**: HTTP methods, stateless design, JSON handling
- ✅ **P2P Networks**: Decentralized architecture, peer discovery
- ✅ **IPC**: Shared memory simulation, synchronization

### **Software Engineering Skills**
- ✅ **Concurrency**: Thread pools, synchronized collections
- ✅ **Network Programming**: Protocol implementation, error handling
- ✅ **System Integration**: Multi-technology coordination
- ✅ **Web Development**: Frontend-backend communication

## Demo Workflow

1. **Start Application**: Run `start_all.bat`
2. **Wait for Startup**: Allow 10-15 seconds for all servers to initialize
3. **Open Interface**: Launch `ui/index.html` in browser
4. **Test Each Module**:
   - Submit complaints and view processing
   - Search room information via RMI
   - Add/view notices through REST API
   - Upload/download files via P2P network
   - Submit feedback and observe live counters

## Conclusion

This project demonstrates **comprehensive understanding of distributed systems** through practical implementation of a realistic hostel utility application. Each module showcases different distributed computing paradigms while maintaining system coherence and performance optimization.

**Key Innovation**: Strategic use of in-memory storage patterns that align with real-world distributed system design principles, proving that **appropriate data lifecycle management** is more valuable than universal persistence.

**Academic Value**: Provides hands-on experience with all major distributed computing concepts in a single, integrated system that students can run, modify, and extend.
