package o.huisang26.mealinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("SetTextI18n")

    InputMethodManager imm;
    EditText editDate;
    TextView diteView;
    TextView timetableView;
    Spinner gradeSpinner;

    private DrawerLayout mDrawerLayout;
    private Context context = this;

    //시도 교육청 코드: R10 표준 학교 코드: 8881025 , 8750130 KEY=178a8938c5404e889f3f20eee3811ae0
    String date_text = null;

    ArrayAdapter<String> gradeAdapter;
    String[] grade = {"중1", "중2", "중3", "고1", "고2", "고3"};

    private SharedPreferences appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.enableDefaults();

        diteView = (TextView)findViewById(R.id.diteText);
        timetableView = (TextView)findViewById(R.id.timetableText);
        editDate = (EditText)findViewById(R.id.editDate);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        gradeSpinner = (Spinner)findViewById(R.id.spinnerGrade);

        //툴바
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false); // 기존 title 지우기
        actionBar.setDisplayHomeAsUpEnabled(true);

        //드로우 레이아웃
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                String title = menuItem.getTitle().toString();

                if(id == R.id.account){
                    Toast.makeText(context, title + "을 눌렀습니다.", Toast.LENGTH_SHORT).show();
                }
                else if(id == R.id.setting){
                    Toast.makeText(context, title + "을 눌렀습니다.", Toast.LENGTH_SHORT).show();
                }
                else if(id == R.id.logout){
                    Toast.makeText(context, title + "을 눌렀습니다.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });


        //gradeSpinner 에 들어갈 어댑터(고1, 고2 등 설정)
        gradeAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                grade);
        gradeSpinner.setAdapter(gradeAdapter);
        gradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Save();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //에딧텍스트를 눌렀을때 기존에 있던 문자 제거
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDate.setText(null);
            }
        });
        //엔터키에 반응
        editDate.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                switch (keyCode){
                    case KeyEvent.KEYCODE_ENTER:
                        date_text = String.valueOf(editDate.getText());
                        HideKeyboard();
                        if (date_text.equals("")){
                            diteView.setText("날짜가 입력되지 않았습니다.");
                            timetableView.setText("");
                            return true;
                        }
                        DiteUpdate();
                        timetableViewUpdate();
                        break;
                }
                return false;
            }
        });
        //바탕 눌렀을 때 키보드 숨기기
        diteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDate.setText(null);
                HideKeyboard();
            }
        });
        View.OnClickListener myClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                HideKeyboard();
            }
        };


        Date currentTime = Calendar.getInstance().getTime();
        date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        Load();
        DiteUpdate();
        timetableViewUpdate();
        HideKeyboard();
    }

    private void DiteUpdate(){
        boolean inDDISH_NM = false, inMMEAL_SC_NM = false;
        String dDISH_NM = null, mMeal_SC_NM = null;
        String code = null;
        boolean inCode = false;

        String[] mealList = {"조식", "중식", "석식"};
        String curMeal = "";
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());

        //아침, 점심, 저녁 순
        Calendar[] mealTime = {Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance()};
        mealTime[0].setTimeInMillis(System.currentTimeMillis());
        mealTime[0].set(Calendar.MINUTE, 20);
        mealTime[0].set(Calendar.HOUR_OF_DAY, 8);
        mealTime[1].setTimeInMillis(System.currentTimeMillis());
        mealTime[1].set(Calendar.MINUTE, 40);
        mealTime[1].set(Calendar.HOUR_OF_DAY, 12);
        mealTime[2].setTimeInMillis(System.currentTimeMillis());
        mealTime[2].set(Calendar.MINUTE, 30);
        mealTime[2].set(Calendar.HOUR_OF_DAY, 6);

        if (currentTime.getTimeInMillis() - mealTime[2].getTimeInMillis() > 0){ //저녁 시간이 넘었는가?
            currentTime.add(Calendar.DATE, 1);
            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            date_text = df.format(currentTime.getTime());
            curMeal = mealList[0];
        } else if(currentTime.getTimeInMillis() - mealTime[1].getTimeInMillis() > 0){ //점심 시간이 넘었는가?
            curMeal = mealList[2];
        } else if(currentTime.getTimeInMillis() - mealTime[0].getTimeInMillis() > 0){ //아침 시간이 넘었는가?
            curMeal = mealList[1];
        } else{
            curMeal = mealList[0];
        }

        diteView.setText(date_text.substring(0, 4) + "년 " +
                date_text.substring(4, 6) + "월 " +
                date_text.substring(6, 8) + "일\n\n");

        try{
            URL url = new URL("https://open.neis.go.kr/hub/mealServiceDietInfo?KEY=178a8938c5404e889f3f20eee3811ae0&Type=xml" +
                    "&pIndex=1&pSize=100&ATPT_OFCDC_SC_CODE=R10&SD_SCHUL_CODE=8881025" +
                    "&MLSV_YMD=" + date_text); //검색 URL부분

            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            parser.setInput(url.openStream(), null);
            int parserEvent = parser.getEventType();

            while (parserEvent != XmlPullParser.END_DOCUMENT){
                switch(parserEvent){
                    case XmlPullParser.START_TAG://parser가 시작 태그를 만나면 실행
                        if(parser.getName().equals(("CODE"))){
                            inCode = true;
                        }
                        if(parser.getName().equals("MMEAL_SC_NM")){
                            inMMEAL_SC_NM = true;
                        }
                        if(parser.getName().equals("DDISH_NM")){
                            inDDISH_NM = true;
                        }

                        break;

                    case XmlPullParser.TEXT://parser가 내용에 접근했을때
                        if (inCode){
                            code = parser.getText();
                            inCode = false;
                        }
                        if(inDDISH_NM){
                            dDISH_NM = parser.getText();
                            inDDISH_NM = false;
                        }
                        if(inMMEAL_SC_NM){
                            mMeal_SC_NM = parser.getText();
                            inMMEAL_SC_NM = false;
                        }

                        break;

                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals("CODE")){
                            if(code.equals("INFO-200")){
                                diteView.setText(diteView.getText() + "오늘은 급식이 없어요 :)");
                            }
                        }
                        if(parser.getName().equals("MMEAL_SC_NM")){
                            if (mMeal_SC_NM.equals(curMeal))
                                diteView.setText(diteView.getText() + mMeal_SC_NM + "\n\n");
                        }
                        if(parser.getName().equals("DDISH_NM")){
                            if (mMeal_SC_NM.equals(curMeal)) {
                                //원래 문자열을 깔끔하게 정리하기 위한 과정
                                String dish = dDISH_NM.replaceAll("[0-9]", "");
                                dish = dish.replace(".", "");
                                dish = dish.replace("*", "");
                                dish = dish.replace("<br/>", "/");
                                String[] dishs = dish.split("/"); //각 급식 항목들을 dishs[]에 하나씩 넣음

                                for (int i = 0; i < dishs.length; i++) {
                                    diteView.setText(diteView.getText() + dishs[i] + "\n");
                                }
                            }
                        }

                        break;
                }
                parserEvent = parser.next();
            }
        } catch(Exception e){
            diteView.setText("에러가.. 났습니다...\n" + e);
        }
    }

    private void timetableViewUpdate(){
        boolean inPerio = false, inITRT_CNTNT = false;
        String perio = null, itrt_CNTNT = null;

        timetableView.setText(null);
        String currentGrade = gradeSpinner.getSelectedItem().toString();
        boolean isHigh = true;
        if (currentGrade.charAt(0) == '중'){
            isHigh = false;
        }
        int grade = Integer.parseInt(currentGrade.substring(1));

        try {
            URL url;
            if (isHigh){
                url = new URL("https://open.neis.go.kr/hub/hisTimetable?KEY=178a8938c5404e889f3f20eee3811ae0&Type=" +
                        "&pIndex=1&pSize=100&ATPT_OFCDC_SC_CODE=R10&SD_SCHUL_CODE=8750130" +
                        "&ALL_TI_YMD=" + date_text +
                        "&GRADE=" + grade);
            } else {
                url = new URL("https://open.neis.go.kr/hub/misTimetable?KEY=178a8938c5404e889f3f20eee3811ae0&Type=xml" +
                        "&pIndex=1&pSize=100&ATPT_OFCDC_SC_CODE=R10&SD_SCHUL_CODE=8881025" +
                        "&ALL_TI_YMD=" + date_text +
                        "&GRADE=" + grade);
            }

            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            parser.setInput(url.openStream(), null);
            int parserEvent = parser.getEventType();

            while (parserEvent != XmlPullParser.END_DOCUMENT){
                switch(parserEvent){
                    case XmlPullParser.START_TAG://parser가 시작 태그를 만나면 실행
                        if(parser.getName().equals(("PERIO"))){
                            inPerio = true;
                        }
                        if(parser.getName().equals("ITRT_CNTNT")){
                            inITRT_CNTNT = true;
                        }
                        break;

                    case XmlPullParser.TEXT://parser가 내용에 접근했을때
                        if (inPerio){
                            perio = parser.getText();
                            inPerio = false;
                        }
                        if (inITRT_CNTNT){
                            itrt_CNTNT = parser.getText();
                            inITRT_CNTNT = false;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals("PERIO")){
                            timetableView.setText(timetableView.getText() + perio + "교시: ");
                        }
                        if(parser.getName().equals("ITRT_CNTNT")){
                            timetableView.setText(timetableView.getText() + itrt_CNTNT.replace("-", "") + "\n");
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch (Exception e) {
        }

    }

    private void  Save(){
        SharedPreferences.Editor editor = appData.edit();

        editor.putInt("GRADE", gradeSpinner.getSelectedItemPosition());

        editor.apply();
    }

    private void Load(){
        gradeSpinner.setSelection(appData.getInt("GRADE", 0));
    }

    private void HideKeyboard() {
        imm.hideSoftInputFromWindow(editDate.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // 왼쪽 상단 버튼 눌렀을 때
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawers();
        } else{
            super.onBackPressed();
        }
    }
}