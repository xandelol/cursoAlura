package br.com.alura.agenda;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.jar.Manifest;

import br.com.alura.agenda.adapter.AlunosAdapter;
import br.com.alura.agenda.converter.AlunoConverter;
import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.modelo.Aluno;

public class ListaAlunosActivity extends AppCompatActivity {

    private ListView listaAlunos;
    private int CODIGO_SMS=321;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_alunos);
        if (checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] { android.Manifest.permission.RECEIVE_SMS } , CODIGO_SMS);
        }
        listaAlunos = (ListView)findViewById(R.id.lista_alunos);

        carregaLista();
        listaAlunos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> lista, View item, int position, long l) {
                Aluno aluno = (Aluno) lista.getItemAtPosition(position);
                Intent intent = new Intent(ListaAlunosActivity.this,FormularioActivity.class);
                intent.putExtra("aluno",aluno);
                startActivity(intent);
            }
        });
        Button novoAluno = (Button) findViewById(R.id.novo_aluno);
        novoAluno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                startActivity(intent);
            }
        });
        registerForContextMenu(listaAlunos);
    }

    private void carregaLista() {
        AlunoDAO dao = new AlunoDAO(this);
        List<Aluno> alunos = dao.buscaAlunos();
        dao.close();

        AlunosAdapter adapter = new AlunosAdapter(this,alunos);
        listaAlunos.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregaLista();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        final Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(info.position);

        MenuItem itemLigar = menu.add("Ligar");
        itemLigar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (ActivityCompat.checkSelfPermission(ListaAlunosActivity.this, android.Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ListaAlunosActivity.this, new String[]{android.Manifest.permission.CALL_PHONE},123);
                }
                else{
                    Intent intentLigar = new Intent(Intent.ACTION_CALL);
                    intentLigar.setData(Uri.parse("tel:"+aluno.getTelefone()));
                    startActivity(intentLigar);
                }
                return false;
            }
        });

        MenuItem itemSite = menu.add("Ir para site");
        Intent intentSite = new Intent(Intent.ACTION_VIEW);
        String site = aluno.getSite();
        if(!site.startsWith("http://")) {
            site = "http://" + site;
        }
        intentSite.setData(Uri.parse(site));
        itemSite.setIntent(intentSite);

        MenuItem itemSMS = menu.add("Sms");
        Intent intentSMS = new Intent(Intent.ACTION_VIEW);
        intentSMS.setData(Uri.parse("sms:" + aluno.getTelefone()));
        itemSMS.setIntent(intentSMS);

        MenuItem itemMapa = menu.add("Vai Para Mapa");
        Intent intentMapa = new Intent(Intent.ACTION_VIEW);
        intentMapa.setData(Uri.parse("geo:0,0?z=14&q=" + aluno.getEndereco()));
        itemMapa.setIntent(intentMapa);

        MenuItem deletar = menu.add("Deletar");
        deletar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item){
                AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                dao.deleta(aluno);
                dao.close();
                carregaLista();
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_alunos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_enviar_notas:
                new EnviaDadosServidor(this).execute();
                break;
            case R.id.menu_baixar_provas:
                Intent vaiParaProvas = new Intent(this, ProvasActivity.class);
                startActivity(vaiParaProvas);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
