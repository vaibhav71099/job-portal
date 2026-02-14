export default function ChatSection({ user, chatPeerId, setChatPeerId, chatMessage, setChatMessage, handleSendMessage, loadConversation, chatMessages }) {
  if (!user) return null;
  return (
    <section className="panel">
      <h2>Chat</h2>
      <form className="card form-grid" onSubmit={handleSendMessage}>
        <input placeholder="Receiver User ID" type="number" value={chatPeerId} onChange={(e) => setChatPeerId(e.target.value)} />
        <textarea placeholder="Type message" value={chatMessage} onChange={(e) => setChatMessage(e.target.value)} />
        <div className="status-buttons">
          <button type="submit">Send</button>
          <button type="button" onClick={() => loadConversation(chatPeerId)}>Load Conversation</button>
        </div>
      </form>

      <div className="grid">
        {chatMessages.length === 0 && <article className="card empty-card"><h3>No messages</h3></article>}
        {chatMessages.map((msg) => (
          <article className="card" key={msg.id}>
            <p><strong>From:</strong> {msg.senderId} <strong>To:</strong> {msg.receiverId}</p>
            <p>{msg.content}</p>
            <p className="muted">{msg.createdAt}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
