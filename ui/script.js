// Module navigation
function showModule(moduleId) {
    // Hide all modules
    document.querySelectorAll('.module').forEach(module => {
        module.classList.remove('active');
    });
    
    // Remove active class from all nav buttons
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Show selected module
    document.getElementById(moduleId).classList.add('active');
    
    // Add active class to clicked button
    event.target.classList.add('active');
    
    // Initialize module-specific functionality
    if (moduleId === 'resources') {
        initP2PModule();
    }
}

// Module 1: Socket Programming - Complaints
document.getElementById('complaintForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const room = document.getElementById('room').value;
    const category = document.getElementById('category').value;
    const description = document.getElementById('description').value;
    
    // Try real socket communication first, fallback to demo mode
    fetch('http://localhost:8082/complaint', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({room, category, description})
    })
    .then(response => response.text())
    .then(data => {
        const parts = data.split('|');
        if (parts[0] === 'SUCCESS') {
            document.getElementById('complaintResult').innerHTML = 
                `<div class="result-item success">${parts[1]}</div>`;
        } else {
            document.getElementById('complaintResult').innerHTML = 
                `<div class="result-item error">${parts[1] || 'Submission failed'}</div>`;
        }
        document.getElementById('complaintForm').reset();
        loadComplaints();
    })
    .catch(error => {
        // Demo mode fallback
        const complaintId = Math.floor(Math.random() * 1000) + 1;
        document.getElementById('complaintResult').innerHTML = 
            `<div class="result-item success">Complaint #${complaintId} submitted successfully! (Demo Mode)</div>`;
        
        // Store in demo storage
        const complaint = {
            id: complaintId,
            room: room,
            category: category,
            description: description,
            date: new Date()
        };
        
        let demoComplaints = JSON.parse(localStorage.getItem('demoComplaints') || '[]');
        demoComplaints.push(complaint);
        localStorage.setItem('demoComplaints', JSON.stringify(demoComplaints));
        
        document.getElementById('complaintForm').reset();
        loadComplaints();
    });
});

function loadComplaints() {
    // Load real complaints from socket server
    fetch('http://localhost:8082/complaints')
    .then(response => response.text())
    .then(data => {
        const parts = data.split('|');
        if (parts[0] === 'LIST') {
            const complaintsData = parts[1];
            if (!complaintsData || complaintsData.trim() === '') {
                document.getElementById('complaintsList').innerHTML = 
                    '<h4>Recent Complaints:</h4><div class="result-item">No complaints submitted yet.</div>';
                return;
            }
            
            const complaints = complaintsData.split(';').filter(c => c.trim() !== '');
            let html = '<h4>Recent Complaints:</h4>';
            
            complaints.forEach(complaintStr => {
                const fields = complaintStr.split(',');
                if (fields.length >= 5) {
                    const id = fields[0];
                    const room = fields[1];
                    const category = fields[2];
                    const description = fields[3];
                    const timestamp = fields.slice(4).join(','); // Handle comma in timestamp
                    
                    html += `<div class="result-item">
                        <strong>ID: #${id}</strong> - Room ${room} (${category})<br>
                        ${description}<br>
                        <small>Submitted: ${timestamp}</small>
                    </div>`;
                }
            });
            
            document.getElementById('complaintsList').innerHTML = html;
        } else {
            document.getElementById('complaintsList').innerHTML = 
                '<h4>Recent Complaints:</h4><div class="result-item error">Failed to load complaints.</div>';
        }
    })
    .catch(error => {
        document.getElementById('complaintsList').innerHTML = 
            '<h4>Recent Complaints:</h4><div class="result-item error">Connection failed. Please ensure servers are running.</div>';
        console.error('Error:', error);
    });
}

// Module 2: RMI - Room Information
function searchRoom() {
    const roomNumber = document.getElementById('roomSearch').value;
    if (!roomNumber) {
        document.getElementById('roomResult').innerHTML = 
            `<div class="result-item error">Please enter a room number</div>`;
        return;
    }
    
    // Real RMI call via bridge
    fetch(`http://localhost:8083/room?number=${roomNumber}`)
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        if (data.error) {
            document.getElementById('roomResult').innerHTML = 
                `<div class="result-item error">${data.error}</div>`;
        } else {
            document.getElementById('roomResult').innerHTML = `
                <div class="result-item success">
                    <h4>Room ${data.room} Information</h4>
                    <p><strong>Occupants:</strong> ${data.occupants}</p>
                    <p><strong>Warden:</strong> ${data.warden}</p>
                    <p><strong>Contact:</strong> ${data.contact}</p>
                </div>
            `;
        }
    })
    .catch(error => {
        console.error('RMI Error:', error);
        document.getElementById('roomResult').innerHTML = 
            `<div class="result-item error">Connection failed: ${error.message}. Check if RMI Bridge (port 8083) is running.</div>`;
    });
}

function loadAllRooms() {
    fetch('http://localhost:8083/rooms')
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        return response.text();
    })
    .then(data => {
        const rooms = data.replace(/[\[\]]/g, '').split(', ');
        let html = '<h4>Available Rooms:</h4>';
        
        rooms.forEach(room => {
            if (room.trim()) {
                html += `<div class="result-item">
                    <strong>Room ${room}</strong>
                    <button onclick="document.getElementById('roomSearch').value='${room}'; searchRoom();">View Details</button>
                </div>`;
            }
        });
        
        document.getElementById('roomResult').innerHTML = html;
    })
    .catch(error => {
        console.error('RMI Error:', error);
        document.getElementById('roomResult').innerHTML = 
            `<div class="result-item error">Connection failed: ${error.message}. Check if RMI Bridge (port 8083) is running.</div>`;
    });
}

// Module 3: REST API - Notices
let currentNoticeRole = 'student';

function showNoticeRole(role) {
    currentNoticeRole = role;
    
    // Update button states
    document.getElementById('adminRoleBtn').classList.toggle('active', role === 'admin');
    document.getElementById('studentRoleBtn').classList.toggle('active', role === 'student');
    
    // Show/hide panels
    document.getElementById('adminPanel').style.display = role === 'admin' ? 'block' : 'none';
    document.getElementById('studentPanel').style.display = role === 'student' ? 'block' : 'none';
    
    // Load appropriate data
    if (role === 'student') {
        loadNotices();
    } else {
        loadNoticesForAdmin();
    }
}

document.getElementById('noticeForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const title = document.getElementById('noticeTitle').value.trim();
    const message = document.getElementById('noticeMessage').value.trim();
    
    if (!title || !message) {
        alert('Please fill in both title and message!');
        return;
    }
    
    // Real REST API call
    fetch('http://localhost:8081/admin', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: `title=${encodeURIComponent(title)}&message=${encodeURIComponent(message)}`
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        return response.text();
    })
    .then(data => {
        alert('✅ Notice published successfully!');
        document.getElementById('noticeForm').reset();
        loadNoticesForAdmin();
        
        // Show success feedback
        const successMsg = document.createElement('div');
        successMsg.className = 'success-message';
        successMsg.innerHTML = '🎉 Notice "' + title + '" has been published!';
        document.getElementById('adminPanel').appendChild(successMsg);
        setTimeout(() => successMsg.remove(), 3000);
    })
    .catch(error => {
        console.error('Notice API Error:', error);
        alert('⚠️ Failed to publish notice. Please check if REST server (port 8081) is running.');
    });
});

function loadNotices() {
    fetch('http://localhost:8081/notices')
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        return response.json();
    })
    .then(notices => {
        displayNoticesForStudents(notices);
    })
    .catch(error => {
        console.error('Notice Load Error:', error);
        document.getElementById('noticesList').innerHTML = 
            '<div class="error-message">⚠️ Failed to load notices. Please check if REST server (port 8081) is running.</div>';
    });
}

function loadNoticesForAdmin() {
    fetch('http://localhost:8081/notices')
    .then(response => response.json())
    .then(notices => {
        document.getElementById('noticeCount').textContent = notices.length;
    })
    .catch(error => {
        document.getElementById('noticeCount').textContent = 'Error';
    });
}

function displayNoticesForStudents(notices) {
    const container = document.getElementById('noticesList');
    
    if (!notices || notices.length === 0) {
        container.innerHTML = '<div class="no-notices">📭 No notices available at the moment.</div>';
        return;
    }
    
    let html = '<div class="notices-header">📢 Latest Hostel Notices</div>';
    
    // Sort notices by ID (newest first)
    notices.sort((a, b) => b.id - a.id);
    
    notices.forEach((notice, index) => {
        const isNew = index < 2; // Mark first 2 as new
        html += `
            <div class="notice-item ${isNew ? 'new-notice' : ''}">
                <div class="notice-header">
                    <h4 class="notice-title">
                        ${isNew ? '🆕 ' : ''}📌 ${notice.title}
                    </h4>
                    <span class="notice-date">📅 ${notice.date}</span>
                </div>
                <div class="notice-content">
                    <p>${notice.message}</p>
                </div>
                <div class="notice-footer">
                    <small>🏫 Notice ID: #${notice.id}</small>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

function clearNoticeView() {
    document.getElementById('noticesList').innerHTML = 
        '<div class="no-notices">📭 View cleared. Click "Load Latest Notices" to see updates.</div>';
}

// Module 4: P2P - Resource Sharing
let p2pNetworkStatus = false;
let currentPeer = 'peer1';

function showPeerView(peer) {
    currentPeer = peer;
    
    // Update button states
    document.getElementById('peer1Btn').classList.toggle('active', peer === 'peer1');
    document.getElementById('peer2Btn').classList.toggle('active', peer === 'peer2');
    
    // Show/hide sections
    document.getElementById('peer1Section').style.display = peer === 'peer1' ? 'block' : 'none';
    document.getElementById('peer2Section').style.display = peer === 'peer2' ? 'block' : 'none';
    
    // Refresh network status for current peer
    refreshNetworkStatus(peer);
}

function refreshNetworkStatus(peer) {
    const statusId = peer === 'peer1' ? 'statusIndicator1' : 'statusIndicator2';
    const peersListId = peer === 'peer1' ? 'peersList1' : 'peersList2';
    
    fetch('http://localhost:8085/p2p/peers')
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(peers => {
        p2pNetworkStatus = true;
        document.getElementById(statusId).innerHTML = '🟢 Connected';
        document.getElementById(statusId).className = 'status-indicator connected';
        
        let peersHtml = '<h5>🤝 Connected Peers:</h5>';
        if (peers.length === 0) {
            peersHtml += '<div class="peer-item">No other peers discovered yet</div>';
        } else {
            peers.forEach(peerAddr => {
                peersHtml += `<div class="peer-item">🖥️ ${peerAddr}</div>`;
            });
        }
        document.getElementById(peersListId).innerHTML = peersHtml;
    })
    .catch(error => {
        p2pNetworkStatus = false;
        document.getElementById(statusId).innerHTML = '🔴 Disconnected';
        document.getElementById(statusId).className = 'status-indicator disconnected';
        document.getElementById(peersListId).innerHTML = 
            '<div class="error-message">⚠️ P2P Bridge not running. Please start P2P services.</div>';
    });
}

function uploadFile(peer) {
    const fileInputId = peer === 'peer1' ? 'fileUpload1' : 'fileUpload2';
    const fileInput = document.getElementById(fileInputId);
    
    if (!fileInput.files.length) {
        alert('📁 Please select a file to upload!');
        return;
    }
    
    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append('file', file, file.name);
    
    fetch('http://localhost:8085/p2p/upload', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(`✅ File "${file.name}" shared successfully from ${peer.toUpperCase()}!`);
            fileInput.value = '';
            listAllFiles(peer);
        } else {
            alert(`⚠️ Upload failed: ${data.message}`);
        }
    })
    .catch(error => {
        alert(`⚠️ P2P Bridge not running. Please start P2P services.`);
    });
}

function searchFiles(peer) {
    const searchInputId = peer === 'peer1' ? 'searchFile1' : 'searchFile2';
    const searchTerm = document.getElementById(searchInputId).value.trim();
    
    if (!searchTerm) {
        alert('🔍 Please enter a search term!');
        return;
    }
    
    fetch(`http://localhost:8085/p2p/search?file=${encodeURIComponent(searchTerm)}`)
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(files => {
        displayFiles(files, `Search results for "${searchTerm}"`, peer);
    })
    .catch(error => {
        const resultsId = peer === 'peer1' ? 'searchResults1' : 'searchResults2';
        document.getElementById(resultsId).innerHTML = 
            '<div class="error-message">⚠️ P2P Bridge not running. Please start P2P services.</div>';
    });
}

function listAllFiles(peer) {
    fetch('http://localhost:8085/p2p/files')
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(files => {
        displayFiles(files, 'All available files on network', peer);
    })
    .catch(error => {
        const resultsId = peer === 'peer1' ? 'searchResults1' : 'searchResults2';
        document.getElementById(resultsId).innerHTML = 
            '<div class="error-message">⚠️ P2P Bridge not running. Please start P2P services.</div>';
    });
}

function displayFiles(files, title, peer) {
    const resultsId = peer === 'peer1' ? 'searchResults1' : 'searchResults2';
    const container = document.getElementById(resultsId);
    
    if (!files || files.length === 0) {
        container.innerHTML = `
            <div class="files-header">${title}</div>
            <div class="no-files">📁 No files found matching your criteria.</div>
        `;
        return;
    }
    
    let html = `<div class="files-header">${title} (${files.length} files)</div>`;
    
    files.forEach(file => {
        const fileIcon = getFileIcon(file.fileName);
        const peerAddress = file.peerAddress || file.peer || 'Unknown';
        
        html += `
            <div class="file-item">
                <div class="file-info">
                    <div class="file-name">
                        ${fileIcon} <strong>${file.fileName}</strong>
                    </div>
                    <div class="file-details">
                        <span class="file-peer">🖥️ ${peerAddress}</span>
                    </div>
                </div>
                <div class="file-actions">
                    <a href="http://localhost:8085/p2p/download?fileName=${encodeURIComponent(file.fileName)}&peerAddress=${encodeURIComponent(peerAddress)}" 
                       download="${file.fileName}" 
                       class="download-link" 
                       onclick="alert('✅ Downloading ${file.fileName} to ${peer.toUpperCase()}!')">
                        <button class="download-btn">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                                <polyline points="7,10 12,15 17,10"/>
                                <line x1="12" y1="15" x2="12" y2="3"/>
                            </svg>
                            Download
                        </button>
                    </a>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

function downloadFile(fileName, peerAddr, peer) {
    // Create a temporary download link
    const downloadUrl = `http://localhost:8085/p2p/download?fileName=${encodeURIComponent(fileName)}&peerAddress=${encodeURIComponent(peerAddr)}`;
    
    // Create invisible anchor element for download
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = fileName;
    link.style.display = 'none';
    
    // Append to body, click, and remove
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    // Show success message
    alert(`✅ Downloading "${fileName}" to ${peer.toUpperCase()}!`);
    
    // Refresh file list
    setTimeout(() => listAllFiles(peer), 1000);
}

function getFileIcon(fileName) {
    const ext = fileName.split('.').pop().toLowerCase();
    switch (ext) {
        case 'pdf': return '📄';
        case 'txt': return '📝';
        case 'doc': case 'docx': return '📘';
        case 'ppt': case 'pptx': return '📊';
        case 'jpg': case 'jpeg': case 'png': case 'gif': return '🖼️';
        case 'mp3': case 'wav': return '🎵';
        case 'mp4': case 'avi': return '🎬';
        case 'zip': case 'rar': return '📦';
        default: return '📄';
    }
}

function initP2PModule() {
    showPeerView('peer1');
}

// Module 5: Shared Memory - Mess Feedback
function submitFeedback(type) {
    fetch('http://localhost:8084/feedback', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: `type=${type}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            alert(`${type.charAt(0).toUpperCase() + type.slice(1)} feedback submitted!`);
            loadFeedbackResults();
        } else {
            alert('Failed to submit feedback: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Feedback Error:', error);
        alert('Connection failed. Please ensure Feedback Bridge (port 8084) is running.');
    });
}

function loadFeedbackResults() {
    fetch('http://localhost:8084/status')
    .then(response => response.json())
    .then(data => {
        document.getElementById('goodCount').textContent = data.good;
        document.getElementById('averageCount').textContent = data.average;
        document.getElementById('poorCount').textContent = data.poor;
        document.getElementById('totalCount').textContent = data.total;
    })
    .catch(error => {
        console.error('Status Load Error:', error);
        document.getElementById('goodCount').textContent = '0';
        document.getElementById('averageCount').textContent = '0';
        document.getElementById('poorCount').textContent = '0';
        document.getElementById('totalCount').textContent = '0';
    });
}

// Initialize with live data from server
window.addEventListener('load', function() {
    loadFeedbackResults();
    // Auto-refresh every 3 seconds for live updates
    setInterval(loadFeedbackResults, 3000);
});