// 改成空字符串，自动适配任何访问地址
const API_BASE = '';
const authContainer = document.getElementById('authContainer');
const chatContainer = document.getElementById('chatContainer');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const authError = document.getElementById('authError');
const authSuccess = document.getElementById('authSuccess');
const btnRegister = document.getElementById('btnRegister');
const btnLogin = document.getElementById('btnLogin');
const btnLogout = document.getElementById('btnLogout');
const currentAgent = document.getElementById('currentAgent');
const agentSelector = document.getElementById('agentSelector');
const messageInput = document.getElementById('messageInput');
const btnSend = document.getElementById('btnSend');
const messagesContainer = document.getElementById('messagesContainer');
const historyList = document.getElementById('historyList');

let currentUserId = null;
let currentAgentRole = 'course';
let chatHistory = {};
let currentConversationId = 'new';
let lastActiveConversationId = null;
let currentActiveSessionId = null;
let contextMenuTargetKey = null;

const agentMap = {
    'course': '课程智能体',
    'training': '培训智能体',
    'cert': '考证智能体',
    'competition': '竞赛智能体'
};

function showError(element, message) {
    element.textContent = message;
    element.style.display = 'block';
    setTimeout(() => {
        element.style.display = 'none';
    }, 3000);
}

function showSuccess(element, message) {
    element.textContent = message;
    element.style.display = 'block';
    setTimeout(() => {
        element.style.display = 'none';
    }, 3000);
}

async function register() {
    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();

    if (!username || !password) {
        showError(authError, '请输入用户名和密码');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/api/user/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (data.code === 200) {
            showSuccess(authSuccess, '注册成功！请登录');
            usernameInput.value = '';
            passwordInput.value = '';
        } else {
            showError(authError, data.msg || '注册失败');
        }
    } catch (error) {
        showError(authError, '网络错误，请稍后重试');
        console.error('注册失败:', error);
    }
}

async function login() {
    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();

    if (!username || !password) {
        showError(authError, '请输入用户名和密码');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/api/user/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (data.code === 200) {
            currentUserId = data.data.id;
            localStorage.setItem('userId', currentUserId.toString());
            localStorage.setItem('username', username);
            
            authContainer.style.display = 'none';
            chatContainer.style.display = 'flex';
            
            await loadChatHistory();
            
            lastActiveConversationId = localStorage.getItem('lastConversationId');
            const savedRole = localStorage.getItem('lastAgentRole');
            if (savedRole) {
                currentAgentRole = savedRole;
                currentAgent.textContent = agentMap[currentAgentRole];
                agentSelector.value = currentAgentRole;
            }
            
            if (lastActiveConversationId && chatHistory[lastActiveConversationId]) {
                loadConversation(lastActiveConversationId, chatHistory[lastActiveConversationId].role);
            }
        } else {
            showError(authError, data.msg || '登录失败');
        }
    } catch (error) {
        showError(authError, '网络错误，请稍后重试');
        console.error('登录失败:', error);
    }
}

function logout() {
    currentUserId = null;
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    chatHistory = {};
    
    messagesContainer.innerHTML = `
        <div class="message ai">
            <div class="avatar ai">AI</div>
            <div class="message-content">
                <div class="message-text">您好！我是您的智能学习助手。请问有什么可以帮助您的？</div>
            </div>
        </div>
    `;
    
    chatContainer.style.display = 'none';
    authContainer.style.display = 'flex';
    usernameInput.value = '';
    passwordInput.value = '';
}

async function loadChatHistory() {
    if (!currentUserId) return;

    try {
        console.log(`加载聊天历史: userId=${currentUserId}`);
        const response = await fetch(`${API_BASE}/api/chat/history/${currentUserId}`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('聊天历史数据:', data);

        if (data.code === 200 && data.data && Array.isArray(data.data)) {
            const history = {};
            
            // 按会话ID分组
            const conversationGroups = {};
            data.data.forEach((item) => {
                const convId = item.conversationId;
                if (!convId) return; // 跳过没有会话ID的记录（旧数据）
                
                if (!conversationGroups[convId]) {
                    conversationGroups[convId] = [];
                }
                conversationGroups[convId].push(item);
            });
            
            // 为每个会话创建一个记录
            Object.keys(conversationGroups).forEach(convId => {
                const items = conversationGroups[convId];
                if (items.length === 0) return;
                
                // 用第一条消息作为会话标题
                const firstItem = items[0];
                const firstMsg = firstItem.userMessage || firstItem.message || '';
                
                // 用第一条消息的角色作为会话角色
                const role = firstItem.agentRole || 'course';
                
                // 用最后一条消息的时间作为会话时间
                const lastItem = items[items.length - 1];
                let lastTimestamp;
                if (lastItem.createTime) {
                    lastTimestamp = new Date(lastItem.createTime).getTime();
                } else if (lastItem.createdAt) {
                    lastTimestamp = new Date(lastItem.createdAt).getTime();
                } else {
                    lastTimestamp = Date.now();
                }
                
                history[convId] = {
                    role: role,
                    title: firstMsg.substring(0, 20) + (firstMsg.length > 20 ? '...' : ''),
                    messages: [],
                    timestamp: lastTimestamp
                };
                
                // 添加所有消息
                items.forEach(item => {
                    const userMsg = item.userMessage || item.message || '';
                    const aiMsg = item.aiReply || item.agentResponse || item.response || '暂无回复';
                    const time = item.createTime || item.createdAt || new Date().toISOString();
                    
                    if (userMsg) {
                        history[convId].messages.push({
                            type: 'user',
                            content: userMsg,
                            time: time
                        });
                    }
                    if (aiMsg) {
                        history[convId].messages.push({
                            type: 'ai',
                            content: aiMsg,
                            time: time
                        });
                    }
                });
            });

            chatHistory = history;
            console.log('处理后的聊天历史:', chatHistory);
            updateHistorySidebar(history);
        } else {
            console.warn('聊天历史数据格式不正确:', data);
        }
    } catch (error) {
        console.error('加载聊天历史失败:', error);
    }
}

function updateHistorySidebar(history) {
    historyList.innerHTML = '';
    
    const newItem = document.createElement('div');
    newItem.className = 'history-item';
    if (currentConversationId === 'new' || (!currentConversationId && !lastActiveConversationId)) {
        newItem.classList.add('active');
    }
    newItem.innerHTML = `
        <div>
            <div class="history-title">新对话</div>
            <div class="history-role">${agentMap[currentAgentRole]}</div>
        </div>
    `;
    newItem.addEventListener('click', () => {
        loadConversation('new', currentAgentRole);
    });
    historyList.appendChild(newItem);

    const sortedHistoryKeys = Object.keys(history).sort((a, b) => {
        const timestampA = history[a]?.timestamp || 0;
        const timestampB = history[b]?.timestamp || 0;
        return timestampB - timestampA;
    });

    sortedHistoryKeys.forEach(key => {
        const item = history[key];
        const historyItem = document.createElement('div');
        historyItem.className = 'history-item';
        if (currentConversationId === key) {
            historyItem.classList.add('active');
        }
        historyItem.dataset.key = key;
        historyItem.innerHTML = `
            <div>
                <div class="history-title" contenteditable="false" data-key="${key}">${item.title}</div>
                <div class="history-role">${agentMap[item.role]}</div>
            </div>
        `;
        historyItem.addEventListener('click', () => {
            loadConversation(key, item.role);
        });
        historyItem.addEventListener('contextmenu', (e) => {
            e.preventDefault();
            showContextMenu(e, key);
        });
        
        // 双击标题编辑
        const titleElement = historyItem.querySelector('.history-title');
        titleElement.addEventListener('dblclick', (e) => {
            e.stopPropagation();
            startEditTitle(titleElement, key);
        });
        
        historyList.appendChild(historyItem);
    });
}

function loadConversation(key, role) {
    document.querySelectorAll('.history-item').forEach(item => {
        item.classList.remove('active');
    });
    
    const items = document.querySelectorAll('.history-item');
    items.forEach(item => {
        if (item.dataset.key === key) {
            item.classList.add('active');
        } else if (key === 'new' && item.textContent.includes('新对话') && !item.dataset.key) {
            item.classList.add('active');
        }
    });

    currentAgentRole = role;
    currentAgent.textContent = agentMap[role];
    agentSelector.value = role;
    currentConversationId = key;
    
    if (key !== 'new') {
        lastActiveConversationId = key;
        localStorage.setItem('lastConversationId', key);
    }
    localStorage.setItem('lastAgentRole', role);

    messagesContainer.innerHTML = '';

    if (key === 'new') {
        messagesContainer.innerHTML = `
            <div class="message ai">
                <div class="avatar ai">AI</div>
                <div class="message-content">
                    <div class="message-text">您好！我是您的${agentMap[role]}。请问有什么可以帮助您的？</div>
                </div>
            </div>
        `;
    } else {
        const history = chatHistory[key];
        if (history && history.messages) {
            history.messages.forEach(msg => {
                addMessage(msg.type, msg.content, msg.time);
            });
        }
    }

    scrollToBottom();
}

function addMessage(type, content, time = null) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    
    const avatarText = type === 'user' ? 'U' : 'AI';
    const avatarClass = type === 'user' ? 'user' : 'ai';
    
    const timeStr = time ? formatTime(time) : formatTime(new Date());
    
    messageDiv.innerHTML = `
        <div class="avatar ${avatarClass}">${avatarText}</div>
        <div class="message-content">
            <div class="message-text">${content}</div>
            <div class="message-time">${timeStr}</div>
        </div>
    `;
    
    messagesContainer.appendChild(messageDiv);
    scrollToBottom();
}

function addTypingIndicator() {
    const typingDiv = document.createElement('div');
    typingDiv.className = 'message ai';
    typingDiv.innerHTML = `
        <div class="avatar ai">AI</div>
        <div class="message-content">
            <div class="typing-indicator">
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
            </div>
        </div>
    `;
    messagesContainer.appendChild(typingDiv);
    scrollToBottom();
    return typingDiv;
}

function removeTypingIndicator(typingDiv) {
    if (typingDiv && typingDiv.parentNode) {
        typingDiv.parentNode.removeChild(typingDiv);
    }
}

async function streamResponse(element, text) {
    element.textContent = '';
    const chars = text.split('');
    for (let i = 0; i < chars.length; i++) {
        element.textContent += chars[i];
        scrollToBottom();
        await new Promise(resolve => setTimeout(resolve, 20 + Math.random() * 30));
    }
}

// 生成新的会话ID
function generateConversationId() {
    return 'conv_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
}

async function sendMessage() {
    const message = messageInput.value.trim();
    if (!message || !currentUserId) return;

    btnSend.disabled = true;

    if (currentConversationId === 'new') {
        currentConversationId = generateConversationId();
    }

    addMessage('user', message);

    messageInput.value = '';

    const typingDiv = addTypingIndicator();

    try {
        // 使用 RAG 接口
        const response = await fetch(`${API_BASE}/api/chat/send-with-rag`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            },
            body: JSON.stringify({
                userId: currentUserId,
                conversationId: currentConversationId,
                agentRole: currentAgentRole,
                message: message
            })
        });

        const data = await response.json();

        removeTypingIndicator(typingDiv);

        if (data.code === 200) {
            const result = data.data;
            const answer = result.answer || '';
            const hasKnowledge = result.hasKnowledge || false;
            const sources = result.sources || [];

            const aiMessageDiv = document.createElement('div');
            aiMessageDiv.className = 'message ai';

            let sourcesHtml = '';
            if (hasKnowledge && sources.length > 0) {
                sourcesHtml = '<div class="sources-info">📚 引用来源：' + sources.map(s => s.docName).join('、') + '</div>';
            }

            aiMessageDiv.innerHTML = `
                <div class="avatar ai">AI</div>
                <div class="message-content">
                    <div class="message-text"></div>
                    <div class="message-time">${formatTime(new Date())}</div>
                    ${sourcesHtml}
                </div>
            `;
            messagesContainer.appendChild(aiMessageDiv);
            scrollToBottom();

            const textElement = aiMessageDiv.querySelector('.message-text');
            await streamResponse(textElement, answer);

            await loadChatHistory();

            lastActiveConversationId = currentConversationId;
            localStorage.setItem('lastConversationId', currentConversationId);
            localStorage.setItem('lastAgentRole', currentAgentRole);

            updateHistorySidebar(chatHistory);
        } else {
            addMessage('ai', data.msg || '抱歉，我无法回答这个问题');
        }
    } catch (error) {
        removeTypingIndicator(typingDiv);
        addMessage('ai', '网络错误，请稍后重试');
        console.error('发送消息失败:', error);
    }

    btnSend.disabled = false;
}

function formatTime(date) {
    if (typeof date === 'string') {
        date = new Date(date);
    }
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
}

function scrollToBottom() {
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function adjustInputHeight() {
    messageInput.style.height = 'auto';
    messageInput.style.height = Math.min(messageInput.scrollHeight, 120) + 'px';
}

function init() {
    const savedUserId = localStorage.getItem('userId');
    if (savedUserId) {
        currentUserId = parseInt(savedUserId);
        authContainer.style.display = 'none';
        chatContainer.style.display = 'flex';
        loadChatHistory();
    }

    btnRegister.addEventListener('click', register);
    btnLogin.addEventListener('click', login);
    btnLogout.addEventListener('click', logout);
    btnSend.addEventListener('click', sendMessage);
    
    agentSelector.addEventListener('change', (e) => {
        currentAgentRole = e.target.value;
        currentAgent.textContent = agentMap[currentAgentRole];
        
        document.querySelectorAll('.history-item').forEach(item => {
            item.classList.remove('active');
        });
        const newItem = document.querySelector('.history-item:first-child');
        if (newItem) {
            newItem.classList.add('active');
            newItem.dataset.agent = currentAgentRole;
            newItem.querySelector('.history-role').textContent = agentMap[currentAgentRole];
        }
        
        currentConversationId = 'new';
        messagesContainer.innerHTML = `
            <div class="message ai">
                <div class="avatar ai">AI</div>
                <div class="message-content">
                    <div class="message-text">您好！我是您的${agentMap[currentAgentRole]}。请问有什么可以帮助您的？</div>
                </div>
            </div>
        `;
    });

    messageInput.addEventListener('input', () => {
        adjustInputHeight();
        btnSend.disabled = !messageInput.value.trim();
    });

    messageInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    adjustInputHeight();
    
    // 右键菜单事件
    document.addEventListener('click', hideContextMenu);
    document.addEventListener('scroll', hideContextMenu);
    
    const renameMenuItem = document.getElementById('renameConversation');
    if (renameMenuItem) {
        renameMenuItem.addEventListener('click', renameConversation);
    }
    
    const deleteMenuItem = document.getElementById('deleteConversation');
    if (deleteMenuItem) {
        deleteMenuItem.addEventListener('click', deleteConversation);
    }
}

// 显示右键菜单
function showContextMenu(e, key) {
    contextMenuTargetKey = key;
    const contextMenu = document.getElementById('contextMenu');
    
    let x = e.clientX;
    let y = e.clientY;
    
    // 确保菜单不会超出视口
    const menuRect = contextMenu.getBoundingClientRect();
    if (x + 160 > window.innerWidth) {
        x = window.innerWidth - 170;
    }
    if (y + 100 > window.innerHeight) {
        y = window.innerHeight - 110;
    }
    
    contextMenu.style.left = x + 'px';
    contextMenu.style.top = y + 'px';
    contextMenu.classList.add('show');
}

// 隐藏右键菜单
function hideContextMenu() {
    const contextMenu = document.getElementById('contextMenu');
    contextMenu.classList.remove('show');
    contextMenuTargetKey = null;
}

// 开始编辑标题
function startEditTitle(element, key) {
    element.contentEditable = true;
    element.focus();
    
    // 选中所有文本
    const range = document.createRange();
    range.selectNodeContents(element);
    const selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
    
    // 添加编辑状态样式
    element.classList.add('editing');
    
    // 保存原始值
    element.dataset.originalValue = element.textContent;
    
    // 绑定事件
    element.addEventListener('blur', () => finishEditTitle(element, key));
    element.addEventListener('keydown', (e) => handleTitleKeydown(e, element, key));
}

// 处理编辑时的按键
function handleTitleKeydown(e, element, key) {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        finishEditTitle(element, key);
    } else if (e.key === 'Escape') {
        e.preventDefault();
        cancelEditTitle(element);
    }
}

// 完成编辑标题
function finishEditTitle(element, key) {
    element.contentEditable = false;
    element.classList.remove('editing');
    
    let newTitle = element.textContent.trim();
    
    if (newTitle === '') {
        // 如果为空，恢复原值
        newTitle = element.dataset.originalValue;
        element.textContent = newTitle;
        return;
    }
    
    // 截断过长的标题
    if (newTitle.length > 20) {
        newTitle = newTitle.substring(0, 20) + '...';
        element.textContent = newTitle;
    }
    
    // 更新内存中的标题
    if (chatHistory[key]) {
        chatHistory[key].title = newTitle;
    }
    
    // 移除事件监听
    element.removeEventListener('blur', () => finishEditTitle(element, key));
    element.removeEventListener('keydown', () => handleTitleKeydown(event, element, key));
}

// 取消编辑标题
function cancelEditTitle(element) {
    element.contentEditable = false;
    element.classList.remove('editing');
    element.textContent = element.dataset.originalValue;
}

// 右键菜单重命名（找到对应元素并启动编辑）
function renameConversation() {
    if (!contextMenuTargetKey) {
        hideContextMenu();
        return;
    }
    
    // 找到对应的标题元素并启动编辑
    const titleElement = document.querySelector(`.history-title[data-key="${contextMenuTargetKey}"]`);
    
    if (titleElement) {
        hideContextMenu();
        // 延迟一下再启动编辑，确保菜单完全关闭
        setTimeout(() => {
            startEditTitle(titleElement, contextMenuTargetKey);
        }, 100);
    } else {
        hideContextMenu();
    }
}

// 删除会话
async function deleteConversation() {
    if (!contextMenuTargetKey || !currentUserId) {
        hideContextMenu();
        return;
    }
    
    if (!confirm('确定要删除这个会话吗？删除后无法恢复！')) {
        hideContextMenu();
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/api/chat/conversation/${currentUserId}/${contextMenuTargetKey}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            // 如果删除的是当前会话，切换到新对话
            if (currentConversationId === contextMenuTargetKey) {
                currentConversationId = 'new';
                lastActiveConversationId = null;
                localStorage.removeItem('lastConversationId');
                
                messagesContainer.innerHTML = `
                    <div class="message ai">
                        <div class="avatar ai">AI</div>
                        <div class="message-content">
                            <div class="message-text">您好！我是您的${agentMap[currentAgentRole]}。请问有什么可以帮助您的？</div>
                        </div>
                    </div>
                `;
            }
            
            // 重新加载历史记录
            await loadChatHistory();
        }
    } catch (error) {
        console.error('删除会话失败:', error);
        alert('删除会话失败，请稍后重试！');
    }
    
    hideContextMenu();
}

document.addEventListener('DOMContentLoaded', init);