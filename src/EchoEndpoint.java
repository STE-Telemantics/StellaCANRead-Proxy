//This endpoint echoes every message received. The Endpoint class defines three lifecycle methods: onOpen, onClose, and onError.
@ServerEndpoint("/echo")
public class EchoEndpoint {
   @OnMessage
   public void onMessage(Session session, String msg) {
      try {
         session.getBasicRemote().sendText(msg);
      } catch (IOException e) {  }
   }
}


// To deploy this programmatic endpoint, use the following code in your Java EE application:
// ServerEndpointConfig.Builder.create(EchoEndpoint.class, "/echo").build();