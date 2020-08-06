package RPC_Server;

public class HelloServiceImp implements HelloService{
    @Override
    public String sayHi(String name) {
        return "Hi " + name;
    }
}
