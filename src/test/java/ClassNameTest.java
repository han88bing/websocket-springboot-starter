import org.junit.Test;
import top.jfunc.websocket.redis.action.RemoveAction;

/**
 * @author xiongshiyan at 2019/4/20 , contact me with email yanshixiong@126.com or phone 15208384257
 */
public class ClassNameTest {
    @Test
    public void testClassName(){
        System.out.println(RemoveAction.class.getName());
        //Spring import的就是这个名字
        System.out.println(RemoveAction.class.getSimpleName());
    }
}
