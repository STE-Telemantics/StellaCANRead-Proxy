@ServerEndpoint("/receive")
public class ReceiveEndpoint {
   @OnMessage
   public void textMessage(Session session, String msg) {
      System.out.println("Text message: " + msg);
   }
   @OnMessage
   public void binaryMessage(Session session, ByteBuffer msg) {
      System.out.println("Binary message: " + msg.toString());
   }
   @OnMessage
   public void pongMessage(Session session, PongMessage msg) {
      System.out.println("Pong message: " + 
                          msg.getApplicationData().toString());
   }
}