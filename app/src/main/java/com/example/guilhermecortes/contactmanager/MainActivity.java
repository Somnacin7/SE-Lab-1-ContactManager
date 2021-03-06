package com.example.guilhermecortes.contactmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.lang3.StringEscapeUtils;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.scottyab.aescrypt.AESCrypt;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements SettingsDialog.OnDialogPositiveListener
{

    public static final String PREFERENCES = "settings";

    private EditText nameTxt, phoneTxt, emailTxt, addressTxt;
    ImageView contactImageImgView;
    List<Contact> Contacts = new ArrayList<Contact>();
    ListView contactListView;
    Uri imageURI = null;

    private String decrypt(String data) {
        try {
            String key = "DrOpP3Db4rB4R14N";
            return AESCrypt.decrypt(key, data);
        }
        catch(Exception e) {
            return ""; //if there was an exception just return ""
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameTxt = (EditText) findViewById(R.id.txtName);
        phoneTxt = (EditText) findViewById(R.id.txtPhone);
        emailTxt = (EditText) findViewById(R.id.txtEmail);
        addressTxt = (EditText) findViewById(R.id.txtAddress);
        contactListView = (ListView) findViewById(R.id.listView);
        contactImageImgView = (ImageView) findViewById(R.id.imgViewContactImage);

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCreator);
        tabSpec.setIndicator("Creator");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("list");
        tabSpec.setContent(R.id.tabContactList);
        tabSpec.setIndicator("List");
        tabHost.addTab(tabSpec);


        // Load Contacts
        try
        {
            if(Sig.verSig("ContactList",getApplicationContext()))
            {
                String fileName = "ContactList";
                FileInputStream fis = getApplicationContext().openFileInput(fileName);
                ObjectInputStream is = new ObjectInputStream(fis);
                List<Contact> loadedContacts = (List<Contact>) is.readObject();
                is.close();
                fis.close();
                Contacts = loadedContacts;
                populateList();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        final Button addBtn = (Button) findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Contacts.add(new Contact(nameTxt.getText().toString(), phoneTxt.getText().toString(), emailTxt.getText().toString(), addressTxt.getText().toString(), imageURI));
                populateList();
                Toast.makeText(getApplicationContext(), nameTxt.getText().toString() +  " has been added to your Contacts!", Toast.LENGTH_SHORT).show();
                clearTextViews();
            }
        });

        nameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            //habilitar o botao se o valor do campo for diferente de vazio
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                addBtn.setEnabled(!nameTxt.getText().toString().trim().isEmpty()); //trim para "cortar os espaços em branco"
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        contactImageImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Contact Image"), 1);
            }
        });

        Intent intent = getIntent();
        String type = intent.getType();

        // Check if they are inserting contact from external intent
        if (type != null && type.equals(ContactsContract.RawContacts.CONTENT_TYPE))
        {

            String name = intent.getStringExtra(ContactsContract.Intents.Insert.NAME);
            String phone = intent.getStringExtra(ContactsContract.Intents.Insert.PHONE);
            String address = intent.getStringExtra("ADDRESS");

            nameTxt.setText(StringEscapeUtils.escapeJava(decrypt(name)));
            phoneTxt.setText(StringEscapeUtils.escapeJava(decrypt(phone)));
            addressTxt.setText(StringEscapeUtils.escapeJava(decrypt(address)));
        }
    }


    public void onActivityResult(int reqCode, int resCode, Intent data){
        if (resCode == RESULT_OK){
            if (reqCode == 1){
                imageURI = data.getData();
                contactImageImgView.setImageURI(data.getData());
            }
        }
    }

    private void populateList(){
        ArrayAdapter<Contact> adapter = new ContactListAdapter();
        contactListView.setAdapter(adapter);
    }

    @Override
    public void OnDialogPositive()
    {
        if (contactListView != null)
        {
            if (contactListView.getAdapter() != null)
            {
                ((ContactListAdapter) contactListView.getAdapter()).notifyDataSetChanged();
            }
        }
    }

    //add contact
//    private void addContact(String name, String phone, String email, String address){
//        Contacts.add(new Contact(name, phone, email, address));
//    }

    private class ContactListAdapter extends ArrayAdapter<Contact>{
        public ContactListAdapter(){
            super (MainActivity.this, R.layout.listview_item, Contacts);
        }

        //criar função para retornar o emelento do array
        @Override
        public View getView(int position, View view, ViewGroup parent){
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_item, parent, false);

            Contact currentContact = Contacts.get(position);

            SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);


            TextView name = (TextView) view.findViewById(R.id.contactName);
            name.setText(currentContact.get_name());

            TextView phone = (TextView) view.findViewById(R.id.phoneNumber);
            if (settings.getBoolean("phoneNumber", false))
            {
                phone.setText(Html.fromHtml("<a href='#'>" + currentContact.get_phone() + "</a>"));
            }
            else
            {
                phone.setText(currentContact.get_phone());
            }

            TextView email = (TextView) view.findViewById(R.id.emailAddress);
            if (settings.getBoolean("email", false))
            {
                email.setText(Html.fromHtml("<a href='#'>" + currentContact.get_email() + "</a>"));
            }
            else
            {
                email.setText(currentContact.get_email());
            }

            TextView address = (TextView) view.findViewById(R.id.cAddress);
            if (settings.getBoolean("address", false))
            {
                address.setText(Html.fromHtml("<a href='#'>" + currentContact.get_address() + "</a>"));
            }
            else
            {
                address.setText(currentContact.get_address());
            }

            ImageView ivContactImage = (ImageView) view.findViewById(R.id.ivContactImage);
            ivContactImage.setImageURI(currentContact.get_imageURI());

            return view;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openSettingsDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openSettingsDialog()
    {
        SettingsDialog settings = new SettingsDialog();
        settings.setListener(this);
        settings.show(getFragmentManager(), "settings");
    }

    private void clearTextViews()
    {
        nameTxt.setText("");
        phoneTxt.setText("");
        emailTxt.setText("");
        addressTxt.setText("");
    }


    // Save contacts
    @Override
    public void onPause() {
        super.onPause();
        try {
            String filename = "ContactList";
            FileOutputStream fos = openFileOutput(filename, getApplicationContext().MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(Contacts);
            os.close();

            Sig.Sign(filename,getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
