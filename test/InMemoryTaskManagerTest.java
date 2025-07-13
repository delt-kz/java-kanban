import manager.InMemoryTaskManager;
import manager.Managers;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createManager() {
        return (InMemoryTaskManager) Managers.getDefault();
    }
}