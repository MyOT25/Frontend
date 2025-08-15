import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myot.R
import com.example.myot.databinding.FragmentSignupStep2Binding
import com.example.myot.signup.SignupFlowActivity
import com.example.myot.signup.SignupStep
import com.example.myot.signup.SignupStep3Fragment
import com.example.myot.signup.data.SignupViewModel

class SignupStep2Fragment : Fragment(), SignupStep {

    private var _binding: FragmentSignupStep2Binding? = null
    private val binding get() = _binding!!
    private var pwVisible = false

    private val vm: SignupViewModel by activityViewModels()
    private val allowedRegex = Regex("^[A-Za-z0-9~!@_#*]+$")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.password.value?.let { binding.etPw.setText(it) }

        binding.etPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUi()
            }
        })

        binding.ivPwToggle.setOnClickListener { togglePwVisible() }

        applyPwVisibility()
        updateUi()
    }

    private fun togglePwVisible() {
        pwVisible = !pwVisible
        applyPwVisibility()
    }

    private fun applyPwVisibility() {
        val sel = binding.etPw.selectionStart
        if (pwVisible) {
            binding.etPw.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.etPw.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.ivPwToggle.setImageResource(R.drawable.ic_eye_on)
        } else {
            binding.etPw.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.etPw.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.ivPwToggle.setImageResource(R.drawable.ic_eye_off)
        }
        binding.etPw.setSelection(maxOf(sel, 0))
    }

    private fun passwordError(pw: String): String? {
        if (pw.isEmpty()) return null
        if (pw.length < 8) return "비밀번호가 8자 미만입니다"
        if (!allowedRegex.matches(pw)) return "사용할 수 없는 기호가 포함되어 있습니다"
        if (!pw.any { it.isLetter() } || !pw.any { it.isDigit() }) {
            return "영문과 숫자를 모두 포함해야 합니다"
        }
        return null
    }

    private fun updateUi() {
        val ctx = requireContext()
        val purple = ContextCompat.getColor(ctx, R.color.point_purple)
        val gray3  = ContextCompat.getColor(ctx, R.color.gray3)

        val pw = binding.etPw.text?.toString().orEmpty()
        val err = passwordError(pw)

        vm.password.value = pw

        // 색상 (유효하면 보라, 아니면 회색 / 미입력 시도 회색)
        binding.etPw.setTextColor(if (err == null && pw.isNotEmpty()) purple else gray3)

        // 에러 표시
        binding.tvPwError.text = err ?: ""
        binding.tvPwError.isVisible = err != null

        // 다음 버튼 활성화
        (activity as? SignupFlowActivity)?.setNextEnabled(err == null && pw.isNotEmpty())
    }

    override fun onNextClicked() {
        val pw = binding.etPw.text?.toString().orEmpty()
        val err = passwordError(pw)
        if (pw.isEmpty() || err != null) return

        vm.password.value = pw

        (activity as? SignupFlowActivity)?.goNext(SignupStep3Fragment())
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}