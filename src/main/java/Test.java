import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class Test {
	public ThreadPoolExecutor mExecutor;
	public Thread mThr;
	
	private static void printThreadInfo(String msg) {
	    String name = Thread.currentThread().getName();
	    System.out.println(name + ": " + msg);
	  }
	public Test(){
		mExecutor = new ScheduledThreadPoolExecutor(1);
	}
	public interface Callback{
		void call(String str);
	}
	public void callDji(Callback cb, long sleepTime, String ret) {
		mExecutor.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				printThreadInfo(ret);
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("callDji");
				cb.call(ret);
			}
			
		});
	}
	public static String blockingCallDji(long sleepTime, String ret) {
		printThreadInfo(ret);
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("blokcingCallDji");
		return ret;
	}
	
	public Future getCallDjiFuture(long sleepTime, String ret) {
		CompletableFuture f = new CompletableFuture();
		callDji(new Callback() {

			@Override
			public void call(String str) {
				// TODO Auto-generated method stub
				f.complete(str);
			}
			
		}, sleepTime, ret);
		return f;
	}
	
	public static Observable getDjiObservable(Test t, long sleepTime, String ret) {
		return Observable.create(new ObservableOnSubscribe<String>() {

			@Override
			public void subscribe(ObservableEmitter<String> emitter) throws Exception {
				// TODO Auto-generated method stub
				t.callDji(new Callback() {

					@Override
					public void call(String str) {
						// TODO Auto-generated method stub
						emitter.onNext(str);
						//emitter.onError(new Throwable("Throwable error"));
						//emitter.onComplete();
					}
				}, sleepTime, ret);
				
			}
			
		});
	}
	
	public static Observable getBlockingDjiObservable(Test t, long sleepTime, String ret) {
		return Observable.create(new ObservableOnSubscribe<String>() {

			@Override
			public void subscribe(ObservableEmitter<String> emitter) throws Exception {
				// TODO Auto-generated method stub
				String retStr = blockingCallDji(sleepTime, ret);
				emitter.onNext(retStr);
			}
			
		});
	}
	public static void main(String[] args) {
		printThreadInfo("main");
		Test t = new Test();
		Observable<String> obs = Observable.just("Hello", "My", "World");
		
		obs.subscribe(s->System.out.println(s));
		
		Observable<String> djiObs = getDjiObservable(t, 3000, "1st dji call");
		Observable<String> djiObs2 = getDjiObservable(t, 2000, "2nd dji call");
		
		Disposable disposable = Observable
			.merge(djiObs,djiObs2)
			/*.onErrorReturn(new Function<Throwable, String>() {
				@Override
				public String apply(Throwable t) throws Exception {
					// TODO Auto-generated method stub
					return t.toString();
				}
	        })*/
			.subscribe(s->System.out.println(s));
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		disposable.dispose();
		
		Observable<String> blockingDjiObs = getDjiObservable(t, 3000, "1st blocking dji call");
		Observable<String> blockingDjiObs2 = getDjiObservable(t, 2000, "2nd blocking dji call");
		
		Disposable disposable2 = Observable
				.merge(blockingDjiObs,blockingDjiObs2)
				/*.onErrorReturn(new Function<Throwable, String>() {
					@Override
					public String apply(Throwable t) throws Exception {
						// TODO Auto-generated method stub
						return t.toString();
					}
		        })*/
				.subscribe(s->System.out.println(s));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		disposable2.dispose();
		//t.mExecutor.shutdownNow();
		/*djiObs
			.onErrorReturn(new Func1<Throwable, String>() {
	            public String call(Throwable throwable) {
	                return "this is an error observable";
	            }
	        })
			.subscribe(s->System.out.println(s));*/
		
		
		
	}
}
