import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Kernel {
	public static final int INTERRUPT_SOFTWARE = 1;
	public static final int INTERRUPT_DISK = 2;
	public static final int INTERRUPT_IO = 3;
	public static final int BOOT = 0;
	public static final int EXEC = 1;
	public static final int WAIT = 2;
	public static final int EXIT = 3;
	public static final int SLEEP = 4;
	public static final int RAWREAD = 5;
	public static final int RAWWRITE = 6;
	public static final int SYNC = 7;
	public static final int READ = 8;
	public static final int WRITE = 9;
	public static final int CREAD = 10;
	public static final int CWRITE = 11;
	public static final int CSYNC = 12;
	public static final int CFLUSH = 13;
	public static final int OPEN = 14;
	public static final int CLOSE = 15;
	public static final int SIZE = 16;
	public static final int SEEK = 17;
	public static final int FORMAT = 18;
	public static final int DELETE = 19;
	public static final int STDIN = 0;
	public static final int STDOUT = 1;
	public static final int STDERR = 2;
	public static final int OK = 0;
	public static final int ERROR = -1;
	private static Scheduler scheduler;
	private static Disk disk;
	private static Cache cache;
	private static SyncQueue waitQueue;
	private static SyncQueue ioQueue;
	private static final int COND_DISK_REQ = 1;
	private static final int COND_DISK_FIN = 2;
	private static BufferedReader input;

	public Kernel() {
	}

	public static int interrupt(int var0, int var1, int var2, Object var3) {
		switch(var0) {
			case 1:
				TCB var4;
				int var8;
				switch(var1) {
					case 0:
						scheduler = new Scheduler();
						scheduler.start();
						disk = new Disk(1000);
						disk.start();
						Disk var10002 = disk;
						cache = new Cache(512, 10);
						ioQueue = new SyncQueue();
						waitQueue = new SyncQueue(scheduler.getMaxThreads());
						return 0;
					case 1:
						return sysExec((String[])((String[])var3));
					case 2:
						if ((var4 = scheduler.getMyTcb()) != null) {
							var8 = var4.getTid();
							return waitQueue.enqueueAndSleep(var8);
						}

						return -1;
					case 3:
						if ((var4 = scheduler.getMyTcb()) != null) {
							var8 = var4.getPid();
							int var9 = var4.getTid();
							if (var8 != -1) {
								waitQueue.dequeueAndWakeup(var8, var9);
								scheduler.deleteThread();
								return 0;
							}
						}

						return -1;
					case 4:
						scheduler.sleepThread(var2);
						return 0;
					case 5:
						while(!disk.read(var2, (byte[])((byte[])var3))) {
							ioQueue.enqueueAndSleep(1);
						}

						while(!disk.testAndResetReady()) {
							ioQueue.enqueueAndSleep(2);
						}

						return 0;
					case 6:
						while(!disk.write(var2, (byte[])((byte[])var3))) {
							ioQueue.enqueueAndSleep(1);
						}

						while(!disk.testAndResetReady()) {
							ioQueue.enqueueAndSleep(2);
						}

						return 0;
					case 7:
						while(!disk.sync()) {
							ioQueue.enqueueAndSleep(1);
						}

						while(!disk.testAndResetReady()) {
							ioQueue.enqueueAndSleep(2);
						}

						return 0;
					case 8:
						switch(var2) {
							case 0:
								try {
									String var5 = input.readLine();
									if (var5 == null) {
										return -1;
									}

									StringBuffer var6 = (StringBuffer)var3;
									var6.append(var5);
									return var5.length();
								} catch (IOException var7) {
									System.out.println(var7);
									return -1;
								}
							case 1:
							case 2:
								System.out.println("threaOS: caused read errors");
								return -1;
							default:
								return -1;
						}
					case 9:
						switch(var2) {
							case 0:
								System.out.println("threaOS: cannot write to System.in");
								return -1;
							case 1:
								System.out.print((String)var3);
								break;
							case 2:
								System.err.print((String)var3);
						}

						return 0;
					case 10:
						return cache.read(var2, (byte[])((byte[])var3)) ? 0 : -1;
					case 11:
						return cache.write(var2, (byte[])((byte[])var3)) ? 0 : -1;
					case 12:
						cache.sync();
						return 0;
					case 13:
						cache.flush();
						return 0;
					case 14:
						return 0;
					case 15:
						return 0;
					case 16:
						return 0;
					case 17:
						return 0;
					case 18:
						return 0;
					case 19:
						return 0;
					default:
						return -1;
				}
			case 2:
				ioQueue.dequeueAndWakeup(2);
				ioQueue.dequeueAndWakeup(1);
				return 0;
			case 3:
				return 0;
			default:
				return 0;
		}
	}

	private static int sysExec(String[] var0) {
		String var1 = var0[0];
		Object var2 = null;

		try {
			Class var3 = Class.forName(var1);
			if (var0.length == 1) {
				var2 = var3.newInstance();
			} else {
				String[] var4 = new String[var0.length - 1];

				for(int var5 = 1; var5 < var0.length; ++var5) {
					var4[var5 - 1] = var0[var5];
				}

				Object[] var13 = new Object[]{var4};
				Constructor var6 = var3.getConstructor(String[].class);
				var2 = var6.newInstance(var13);
			}

			Thread var12 = new Thread((Runnable)var2);
			TCB var14 = scheduler.addThread(var12);
			return var14 != null ? var14.getTid() : -1;
		} catch (ClassNotFoundException var7) {
			System.out.println(var7);
			return -1;
		} catch (NoSuchMethodException var8) {
			System.out.println(var8);
			return -1;
		} catch (InstantiationException var9) {
			System.out.println(var9);
			return -1;
		} catch (IllegalAccessException var10) {
			System.out.println(var10);
			return -1;
		} catch (InvocationTargetException var11) {
			System.out.println(var11);
			return -1;
		}
	}

	static {
		input = new BufferedReader(new InputStreamReader(System.in));
	}
}
