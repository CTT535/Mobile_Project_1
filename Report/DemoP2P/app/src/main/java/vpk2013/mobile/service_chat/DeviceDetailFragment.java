package vpk2013.mobile.service_chat;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.net.InetAddress;

/**
 * Created by VUDAI on 5/9/2017.
 */

public class DeviceDetailFragment extends Fragment {
    private View m_contentView = null;
    private DeviceInfo m_device;
    ProgressDialog progressDialog = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        m_contentView = inflater.inflate(R.layout.device_detail, null);
        m_contentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                InetAddress deviceAddress = m_device.getIpAdress();
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + deviceAddress, true, true
                );

            }
        });

        m_contentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                    }
                });

        return m_contentView;
    }


    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(DeviceInfo device) {
        this.m_device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) m_contentView.findViewById(R.id.device_address);
        view.setText(device.getIpAdress().toString());
        view = (TextView) m_contentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        m_contentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) m_contentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) m_contentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) m_contentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        this.getView().setVisibility(View.GONE);
    }

    public class DeviceInfo{
        protected String m_deviceName;
        protected InetAddress m_ipAdress;
        protected int m_state;
        public final int CONNECTED = 1;
        public final int NOT_CONNECTED = 2;
        public DeviceInfo(InetAddress ipAdress, String deviceName){
            m_ipAdress = ipAdress;
            m_deviceName = deviceName;
            m_state =  NOT_CONNECTED;
        }
        
        public void setState(int state){
            m_state = state;
        }
        
        public boolean isConnected(){
            return m_state == CONNECTED;
        }

        public InetAddress getIpAdress(){
            return  m_ipAdress;
        }

        public String geDeviceName(){
            return m_deviceName;
        }

        public String getState(){
            if (m_state == CONNECTED){
                return "Connected";
            }
            return "avaible";
        }
        
    }

}
