package com.czhj.devicehelper.oaId.interfaces;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.util.Log;

import com.czhj.sdk.logger.SigmobLog;

/****************************
 * on 2020/2/17
 ****************************
 */
public interface OnePlusIDInterface extends IInterface{

    abstract class up extends Binder implements OnePlusIDInterface {

    public static class down implements OnePlusIDInterface {
      
      public IBinder iBinder;

      public down(IBinder ib) {
        iBinder = ib;
      }

      public String getSerID(String str1, String str2, String str3) {
        String res = null;
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
          obtain.writeInterfaceToken("com.heytap.openid.IOpenID");
          obtain.writeString(str1);
          obtain.writeString(str2);
          obtain.writeString(str3);
          iBinder.transact(1, obtain, obtain2, 0);
          obtain2.readException();
          res = obtain2.readString();
          Log.d("oaid", "getSerID() called with: res = [" +res + "]");

        }
        catch (Exception e) {
          Log.d("oaid", "getSerID() called with: Exception = [" + e.getMessage() + "]");

          SigmobLog.e(e.getMessage());
        }
        finally {
          obtain.recycle();
          obtain2.recycle();
        }
        return res;
      }

      @Override
      public IBinder asBinder() {
        return iBinder;
      }
    }

    public static OnePlusIDInterface genInterface(IBinder iBinder) {
      if (iBinder == null) {
        return null;
      }
      IInterface iInterface = iBinder.queryLocalInterface("com.heytap.openid.IOpenID");
      if (iInterface == null || !(iInterface instanceof OnePlusIDInterface)) {
        return new up.down(iBinder);
      }
      else {
        return (OnePlusIDInterface) iInterface;
      }
    }
  }
}
