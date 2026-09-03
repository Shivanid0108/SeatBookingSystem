package module;

import java.time.LocalDateTime;

/**
 * Message represents a direct message between two users.
 * 
 * FROM ASSIGNMENT: "Users can communicate via an internal messaging service and
 * bargain for seat prices on the sell list." "Any user can send a complaint to
 * the admin by using the internal messaging service."
 * 
 * DIFFERENCE between Message and Notification: - Message = USER to USER
 * (manual, conversational) - Notification = SYSTEM to USER (automatic,
 * event-triggered)
 * 
 * USE CASES: 1. Buyer messages seller: "Would you take $80 for seat 3C?" 2.
 * Seller responds: "Lowest I can go is $90" 3. User messages Admin: "This venue
 * has poor service, please investigate."
 * 
 * RELATIONSHIPS: Message HAS-ONE sender (User) Message HAS-ONE receiver (User)
 * 
 * WHY both sender and receiver are User (not ClientUser)? Because Admin can
 * also send and receive messages. Complaints go TO Admin, Admin might reply
 * back. Using parent User type supports all user types.
 * 
 * SPRING BOOT NOTE: - @Entity - @ManyToOne on sender and receiver
 */
public class Message {

	/**
	 * WHY Long id? Spring Boot JPA standard.
	 */
	private Long id;

	/**
	 * WHY store full User objects for sender and receiver? We need their details
	 * (name, email) to display conversations. Just storing IDs would require extra
	 * DB lookups.
	 * 
	 * In Spring Boot:
	 * 
	 * @ManyToOne
	 * @JoinColumn(name = "sender_id") private User sender;
	 */
	private User sender;
	private User receiver;

	/**
	 * WHY LocalDateTime timestamp? Messages need to be sorted chronologically in a
	 * conversation. "Sent at 3:45pm" is important context for the reader. Auto-set
	 * to now() when message is created.
	 */
	private LocalDateTime timestamp;

	/**
	 * WHY String content? The actual message text typed by the sender. No length
	 * limit in model — UI layer can enforce that.
	 */
	private String content;

	/**
	 * WHY boolean isRead? Receiver needs to know which messages they haven't read
	 * yet. Like email — unread count in inbox. Starts false, set to true when
	 * receiver opens the message.
	 */
	private boolean isRead;

	/**
	 * Constructor — called by messaging service when user sends a message.
	 * 
	 * WHY only sender, receiver, content as parameters? - timestamp: always now()
	 * automatically - isRead: always starts false - id: auto-generated
	 */
	public Message(User sender, User receiver, String content) {
		this.sender = sender;
		this.receiver = receiver;
		this.content = content;
		this.timestamp = LocalDateTime.now();
		this.isRead = false;
	}

	public Long getId() {
		return id;
	}

	public User getSender() {
		return sender;
	}

	public User getReceiver() {
		return receiver;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public String getContent() {
		return content;
	}

	public boolean isRead() {
		return isRead;
	}

	/**
	 * WHY setter only for isRead? Same reason as Notification — only read status
	 * changes. sender, receiver, content, timestamp never change after sending.
	 */
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	@Override
	public String toString() {
		return sender.getName() + " → " + receiver.getName() + " [" + timestamp + "]: " + content;
	}
}