public class EchoEndpoint extends Endpoint {
    @Override
    public void onOpen(final Session session, EndpointConfig config) {
       session.addMessageHandler(new MessageHandler.Whole<String>() {
          @Override
          public void onMessage(String msg) {
             try {
                session.getBasicRemote().sendText(msg);
             } catch (IOException e) { 
                
              }
          }
       });
    }
 }