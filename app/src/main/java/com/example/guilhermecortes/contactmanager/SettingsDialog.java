package com.example.guilhermecortes.contactmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListAdapter;

import java.util.Set;

/**
 * Created by Somn on 1/26/2016.
 */
public class SettingsDialog extends DialogFragment
{
    private OnDialogPositiveListener listener;

    public SettingsDialog()
    {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFERENCES, 0);

        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_settings, null);

        CheckBox phoneNumber = (CheckBox) view.findViewById(R.id.settings_phone_number_checkbox);
        CheckBox email = (CheckBox) view.findViewById(R.id.settings_email_checkbox);
        CheckBox address = (CheckBox) view.findViewById(R.id.settings_address_checkbox);

        phoneNumber.setChecked(settings.getBoolean("phoneNumber", false));
        email.setChecked(settings.getBoolean("email", false));
        address.setChecked(settings.getBoolean("address", false));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle("Settings")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        SharedPreferences.Editor editor = settings.edit();

                        boolean phoneNumber = ((CheckBox) view.findViewById(R.id.settings_phone_number_checkbox)).isChecked();
                        boolean email = ((CheckBox) view.findViewById(R.id.settings_email_checkbox)).isChecked();
                        boolean address = ((CheckBox) view.findViewById(R.id.settings_address_checkbox)).isChecked();

                        editor.putBoolean("phoneNumber", phoneNumber);
                        editor.putBoolean("email", email);
                        editor.putBoolean("address", address);

                        editor.apply();

                        listener.OnDialogPositive();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });

        return builder.create();
    }

    public static interface OnDialogPositiveListener
    {
        void OnDialogPositive();
    }

    public void setListener(OnDialogPositiveListener listener)
    {
        this.listener = listener;
    }

}
